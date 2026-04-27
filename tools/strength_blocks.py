import json
import os

def process_minecraft_configs(blocks):
    """
    根据方块的名称和现有分类，推断其物理类型并添加强度参数。
    """
    processed_blocks = {}

    for name, data in blocks.items():
        # 复制原始数据
        new_data = data.copy()
        
        # 获取基础信息
        category = data.get("category", "")
        
        # 定义推断逻辑：[类型名称, 硬度, 抗爆性]
        # 默认值
        block_type = "unknown"
        hardness = 1.0
        resistance = 1.0

        # 1. 流体类 (空气、水、岩浆、果汁)
        if data.get("fluid") or name in ["air", "water", "lava01", "lava02"] or "juice" in name or name == "milk":
            block_type = "fluid"
            hardness = 0.0
            resistance = 0.0
            
        # 2. 装饰类 (字母、数字、符号)
        elif category in ["letter", "number", "symbol"]:
            block_type = "decorative_plastic"
            hardness = 0.8
            resistance = 0.8
            
        # 3. 自然/土质类 (泥土、沙子、草地、雪)
        elif category in ["nature", "element"] and any(k in name for k in ["dirt", "grass", "sand", "snow", "clay"]):
            block_type = "soil"
            hardness = 0.5
            resistance = 0.5
            
        # 4. 木质类 (木板、木头、木箱)
        elif "wood" in name or "plank" in name or "acacia" in name or "bamboo" in name or "box" in name:
            block_type = "wood"
            hardness = 2.0
            resistance = 3.0
            
        # 5. 石质/建筑类 (石头、砖块、墙、柱子、石英)
        elif any(k in name for k in ["stone", "brick", "wall", "pillar", "quartz", "palace", "rock"]):
            block_type = "rock"
            hardness = 1.5
            resistance = 6.0 # 石头类抗爆性较高
            
        # 6. 玻璃/透明类 (窗户、玻璃、装饰灯)
        elif data.get("transparent") or any(k in name for k in ["glass", "window", "lamp", "lantern"]):
            block_type = "glass"
            hardness = 0.3
            resistance = 0.3
            
        # 7. 食物类 (蛋糕、饼干、糖和果)
        elif category == "food":
            block_type = "food"
            hardness = 0.5
            resistance = 0.5
            
        # 8. 金属/工业类 (不锈钢、传送带、实验室材料)
        elif "steel" in name or "conveyor" in name or "lab_material" in name:
            block_type = "metal"
            hardness = 5.0
            resistance = 6.0
            
        # 9. 纯颜色块
        elif category == "color":
            block_type = "paint"
            hardness = 1.0
            resistance = 1.0

        if data.get("transparent"):
            if hardness>=0.2:
                hardness -= 0.2
            if resistance>=0.2:
                resistance -= 0.2
        
        # 写入推断的类型
        new_data["type"] = block_type
        
        # 写入强度参数 (包含硬度和抗爆性)
        new_data["strength"] = {
            "hardness": hardness,
            "resistance": resistance
        }
        
        processed_blocks[name] = new_data

    return processed_blocks

# 执行处理
if __name__ == "__main__":
    # 获取脚本所在的目录，确保无论从哪里运行都能找到 JSON 文件
    base_dir = os.path.dirname(os.path.abspath(__file__))
    
    input_file = os.path.join(base_dir, 'block-spec.json')
    output_file = os.path.join(base_dir, 'block-spec.json')

    # 检查文件是否存在
    if os.path.exists(input_file):
        try:
            with open(input_file, 'r', encoding='utf-8') as f:
                raw_json_data = json.load(f)
            
            # 处理数据
            result = process_minecraft_configs(raw_json_data)
            
            # 打印处理结果
            print(f"Successfully processed {len(result)} blocks.")
            
            # 保存到新文件
            with open(output_file, 'w', encoding='utf-8') as f:
                json.dump(result, f, indent=2, ensure_ascii=False)
            print(f"Results saved to: {output_file}")

        except json.JSONDecodeError:
            print(f"Error: Failed to decode JSON from {input_file}. Please check the file format.")
        except Exception as e:
            print(f"An unexpected error occurred: {e}")
    else:
        print(f"Error: The file 'block-spec.json' was not found at {input_file}.")
        print("Please ensure the JSON file is in the same folder as this script.")