import json
from pathlib import Path

# 模组 ID，用于生成资源路径前缀
MOD_ID = "box3"

# 当前 py 文件目录
BASE_DIR = Path(__file__).parent

# 贴图目录
TEXTURES_DIR = BASE_DIR / "assets" / MOD_ID / "textures" / "block"

# 六个面对应的后缀
FACES = ["top", "bottom", "front", "back", "left", "right"]


def normalize_texture_filenames_to_lowercase():
    files = sorted(TEXTURES_DIR.glob("*.png"))
    if not files:
        return

    lower_to_paths = {}
    for p in files:
        lower_to_paths.setdefault(p.name.lower(), []).append(p)

    collisions = {k: v for k, v in lower_to_paths.items() if len(v) > 1}
    if collisions:
        print("⚠️  发现小写重名贴图文件，已跳过重命名这些文件:")
        for lower_name, paths in sorted(collisions.items()):
            print(f"  - {lower_name}: {', '.join(x.name for x in paths)}")

    for p in files:
        lower_name = p.name.lower()
        if lower_name == p.name:
            continue
        if lower_name in collisions:
            continue

        target = p.with_name(lower_name)
        if target.exists():
            print(f"⚠️  目标文件已存在，跳过重命名: {p.name} -> {target.name}")
            continue
        p.rename(target)


def scan_texture_parts():
    """扫描贴图目录，提取所有方块的基础名称（texture_part）。"""
    texture_parts = set()
    for file_path in TEXTURES_DIR.glob("*.png"):
        parts = file_path.stem.split("_")
        if len(parts) >= 2 and parts[-1] in FACES:
            texture_part = "_".join(parts[:-1]).lower()
            if texture_part:
                texture_parts.add(texture_part)
            continue

        if len(parts) >= 3 and parts[-2] in FACES and parts[-1].isdigit():
            texture_part = "_".join(parts[:-2]).lower()
            if texture_part:
                texture_parts.add(texture_part)
    return sorted(texture_parts)


def check_all_faces_exist(texture_part):
    """检查指定 texture_part 的六个面贴图是否都存在。"""
    missing = []
    for face in FACES:
        texture_file = TEXTURES_DIR / f"{texture_part}_{face}.png"
        legacy_texture_file = TEXTURES_DIR / f"{texture_part}_{face}_0.png"
        if not texture_file.exists() and not legacy_texture_file.exists():
            missing.append(texture_file)
    return missing


def generate_blockstate(texture_part):
    """生成指定方块的 blockstate JSON 数据结构。"""
    # 带水平朝向的方块状态，配合 Java 里的 HORIZONTAL_FACING 使用
    return {
        "variants": {
            "facing=north": {"model": f"{MOD_ID}:block/{texture_part}"},
            "facing=east":  {"model": f"{MOD_ID}:block/{texture_part}", "y": 90},
            "facing=south": {"model": f"{MOD_ID}:block/{texture_part}", "y": 180},
            "facing=west":  {"model": f"{MOD_ID}:block/{texture_part}", "y": 270}
        }
    }


def generate_block_model(texture_part):
    """生成指定方块的方块模型 JSON 数据结构，统一使用 cube 并按规则生成六面贴图。"""
    return {
        "parent": "minecraft:block/cube",
        "textures": {
            "up":    f"{MOD_ID}:block/{texture_part}_top",
            "down":  f"{MOD_ID}:block/{texture_part}_bottom",
            "north": f"{MOD_ID}:block/{texture_part}_front",
            "south": f"{MOD_ID}:block/{texture_part}_back",
            "west":  f"{MOD_ID}:block/{texture_part}_left",
            "east":  f"{MOD_ID}:block/{texture_part}_right",
            "particle": f"{MOD_ID}:block/{texture_part}_bottom"
        }
    }


def generate_item_model(texture_part):
    """生成指定方块对应物品的模型 JSON 数据结构。"""
    return {
        "model": {
            "type": "minecraft:model",
            "model": f"{MOD_ID}:block/{texture_part}"
        }
    }



def pretty_display_name(name: str) -> str:
    tokens = [t for t in name.split("_") if t]
    pretty_tokens = []
    for t in tokens:
        if t.isalpha():
            pretty_tokens.append(t[:1].upper() + t[1:])
        else:
            pretty_tokens.append(t)
    return " ".join(pretty_tokens)


def main():
    """脚本入口：扫描贴图目录并为每个方块生成所有 JSON 文件。"""
    normalize_texture_filenames_to_lowercase()
    texture_parts = scan_texture_parts()
    print(f"发现 {len(texture_parts)} 个方块: {', '.join(texture_parts)}")

    lang = {}
    block_id_mapping = {}  # 新增：ID 到注册 key 的映射
    
    # 读取 block-spec.json 获取 ID 信息
    with open(BASE_DIR / "block-spec.json", "r", encoding="utf-8") as f:
        block_spec = json.load(f)
    
    for texture_part in texture_parts:
        # 检查六个面贴图是否都存在
        missing = check_all_faces_exist(texture_part)
        if missing:
            print(f"⚠️  {texture_part} 缺少贴图: {', '.join(m.name for m in missing)}")
            continue
        
        # 生成三种 JSON 文件
        block_name = f"{texture_part}"

        lang[f"block.{MOD_ID}.{block_name}"] = pretty_display_name(texture_part)

        configs = [
            (BASE_DIR / "assets" / MOD_ID / "blockstates" / f"{block_name}.json",
             generate_blockstate(texture_part)),
            (BASE_DIR / "assets" / MOD_ID / "models" / "block" / f"{block_name}.json",
             generate_block_model(texture_part)),
            (BASE_DIR / "assets" / MOD_ID / "items" / f"{block_name}.json",
             generate_item_model(texture_part)),
        ]
        
        for path, data in configs:
            path.parent.mkdir(parents=True, exist_ok=True)
            with open(path, "w") as f:
                json.dump(data, f, indent=2)
        
        print(f"✅ Generated: {block_name}")

    # 处理 block-spec.json 中的所有方块来生成 ID 映射
    for name, props in block_spec.items():
        block_id = props.get("id")
        if block_id is not None:
                block_id_mapping[str(block_id)] = f"{name}"

    # 生成 block-id.json 文件
    block_id_path = BASE_DIR / "block-id.json"
    with open(block_id_path, "w", encoding="utf-8") as f:
        # 按 ID 排序
        sorted_mapping = dict(sorted(block_id_mapping.items(), key=lambda x: int(x[0])))
        json.dump(sorted_mapping, f, indent=2, ensure_ascii=False)
    print(f"✅ Generated: block-id.json ({len(sorted_mapping)} entries)")

    # 合并 lang
    # lang_path = BASE_DIR / "assets" / MOD_ID / "lang" / "en_us.json"
    # lang_path.parent.mkdir(parents=True, exist_ok=True)
    # if lang_path.exists():
    #     with open(lang_path, "r", encoding="utf-8") as f:
    #         existing_lang = json.load(f)
    # else:
    #     existing_lang = {}
    # existing_lang.update(lang)
    # with open(lang_path, "w", encoding="utf-8") as f:
    #     json.dump(existing_lang, f, ensure_ascii=False, indent=2, sort_keys=True)
    # print(f"✅ Generated: lang/en_us.json ({len(existing_lang)} entries)")


if __name__ == "__main__":
    main()