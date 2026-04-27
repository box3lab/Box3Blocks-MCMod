import sharp from "sharp";
import {
  FaceName,
  getIds,
  getNameById,
  getSoildBlockTexturePositionById,
  isFluid,
} from "./blockIndex.js";
import { mkdirSync, writeFileSync } from "node:fs";

const files = {
  material: "inputs/material.png",
  bump: "inputs/bump.png",
  color: "inputs/color.png",
};

const sharpObj = {
  material: sharp(files.material),
  bump: sharp(files.bump),
  color: sharp(files.color),
};

const mcAnimationConfig = {
  defaultFrameTime: 4,
  frameTimeOverrides: {} as Record<string, number>,
};

/* top: 顶部
 * bottom: 底部
 * cover: 顶部 & 底部
 * left: 左边
 * right: 右边
 * foward: 前边
 * backward: 后边
 * side: 前后左右环绕
 * all: 全部
 *
 * [注意！] 有all或side描述的情况下，可以定义某一面的特殊情况
 */
const tilesPositionMap: Map<[number, number], string> = new Map(); // new Map([
//     // lava01
//     [[0, 0], "lava01_all_00"],
//     [[1, 0], "lava01_all_01"],
//     [[2, 0], "lava01_all_02"],
//     [[3, 0], "lava01_all_03"],
//     [[4, 0], "lava01_top_00"], // override
//     [[5, 0], "lava01_top_01"],
//     [[6, 0], "lava01_top_02"],
//     [[7, 0], "lava01_top_03"],
//     // lava02
//     [[8, 0], "lava02_all_00"],
//     [[9, 0], "lava02_all_01"],
//     [[10, 0], "lava02_all_02"],
//     [[11, 0], "lava02_all_03"],
//     [[12, 0], "lava02_top_00"], // override
//     [[13, 0], "lava02_top_01"],
//     [[14, 0], "lava02_top_02"],
//     [[15, 0], "lava02_top_03"],
//     // windygrass
//     [[16, 0], "windygrass_top_00"], // override
//     [[17, 0], "windygrass_top_01"],
//     [[18, 0], "windygrass_top_02"],
//     [[19, 0], "windygrass_top_03"],
//     // conveyor
//     [[20, 0], "conveyer_top_00"],
//     [[21, 0], "conveyer_top_01"],
//     [[22, 0], "conveyer_top_02"],
//     [[23, 0], "conveyer_top_03"],
//     [[23, 10], "conveyer_side"],
//     [[24, 10], "conveyer_bottom"],
//     // ledfloor01
//     [[24, 0], "ledfloor01_all_00"],
//     [[25, 0], "ledfloor01_all_01"],
//     [[26, 0], "ledfloor01_all_02"],
//     [[27, 0], "ledfloor01_all_03"],
//     // ledfloor02
//     [[28, 0], "ledfloor02_all_00"],
//     [[29, 0], "ledfloor02_all_01"],
//     [[30, 0], "ledfloor02_all_02"],
//     [[31, 0], "ledfloor02_all_03"],
//     // star_lamp
//     [[0, 1], "star_lamp_all_00"],
//     [[1, 1], "star_lamp_all_01"],
//     [[2, 1], "star_lamp_all_02"],
//     [[3, 1], "star_lamp_all_03"],
//     // snowflake_lamp
//     [[4, 1], "snowflake_lamp_all_00"],
//     [[5, 1], "snowflake_lamp_all_01"],
//     [[6, 1], "snowflake_lamp_all_02"],
//     [[7, 1], "snowflake_lamp_all_03"],
//     // blue_decorative_light
//     [[8, 1], "blue_decorative_light_all_00"],
//     [[9, 1], "blue_decorative_light_all_01"],
//     [[10, 1], "blue_decorative_light_all_02"],
//     [[11, 1], "blue_decorative_light_all_03"],
//     // green_decorative_light
//     [[12, 1], "green_decorative_light_all_00"],
//     [[13, 1], "green_decorative_light_all_01"],
//     [[14, 1], "green_decorative_light_all_02"],
//     [[15, 1], "green_decorative_light_all_03"],
//     // red_decorative_light
//     [[16, 1], "red_decorative_light_all_00"],
//     [[17, 1], "red_decorative_light_all_01"],
//     [[18, 1], "red_decorative_light_all_02"],
//     [[19, 1], "red_decorative_light_all_03"],
//     // yellow_decorative_light
//     [[20, 1], "yellow_decorative_light_all_00"],
//     [[21, 1], "yellow_decorative_light_all_01"],
//     [[22, 1], "yellow_decorative_light_all_02"],
//     [[23, 1], "yellow_decorative_light_all_03"],
//     // rainbow_cube
//     [[24, 1], "rainbow_cube_all_00"],
//     [[25, 1], "rainbow_cube_all_01"],
//     [[26, 1], "rainbow_cube_all_02"],
//     [[27, 1], "rainbow_cube_all_03"],
//     // firecracker
//     [[28, 1], "firecracker_side_00"],
//     [[29, 1], "firecracker_side_01"],
//     [[30, 1], "firecracker_side_02"],
//     [[31, 1], "firecracker_side_03"],
//     [[0, 0], "firecracker_cover_00"],
//     [[1, 0], "firecracker_cover_01"],
//     [[2, 0], "firecracker_cover_02"],
//     [[3, 0], "firecracker_cover_03"],
// ]);
getIds().forEach((id) => {
  if (isFluid(id)) {
    console.log(`Skipping fluid ${getNameById(id)}<${id}>`);
    return;
  }
  const name = getNameById(id);
  const blockTexture = getSoildBlockTexturePositionById(id);
  console.log(`- Block ${name}`);
  (Object.keys(blockTexture) as Array<FaceName>).forEach((faceName) => {
    const face = blockTexture[faceName];
    face.forEach((position, frame) => {
      tilesPositionMap.set(
        [position.x, position.y],
        `${name}_${faceName}_${frame}`.toLowerCase(),
      );
      console.log(
        `Config ${name}_${faceName}_${frame} at ${position.x} ${position.y}`,
      );
    });
  });
});

async function processFile(
  image: sharp.Sharp,
  map: Map<[number, number], string>,
  type: string,
) {
  mkdirSync(`./out/${type}/`, { recursive: true });
  mkdirSync(`./out-mc/${type}/`, { recursive: true });
  const metadata = await image.metadata();
  const unitWidth = Math.round((metadata.width ?? 0) / 32);
  const unitHeight = Math.round((metadata.height ?? 0) / 32);
  console.log(unitWidth);
  console.log(unitHeight);

  const tileFrames: Map<
    string,
    Array<{ frame: number; buffer: Buffer }>
  > = new Map();

  const tasks: Array<Promise<void>> = [];
  map.forEach((tileName, position) => {
    tasks.push(
      (async () => {
        console.log(
          `Cutting ${type} ${tileName} at <${position[0]}, ${position[1]}>`,
        );
        const config = {
          width: unitWidth,
          height: unitHeight,
          left: position[0] * unitWidth,
          top: position[1] * unitHeight,
        };
        console.log(config);
        const result = await image.clone().extract(config);

        const legacyPath = `out/${type}/${tileName}.png`.toLowerCase();
        await result.clone().toFile(legacyPath);

        const match = tileName.match(/^(.*)_(\d+)$/);
        const baseName = (match?.[1] ?? tileName).toLowerCase();
        const frame = Number(match?.[2] ?? 0);
        const buffer = await result.clone().png().toBuffer();

        const list = tileFrames.get(baseName) ?? [];
        list.push({ frame, buffer });
        tileFrames.set(baseName, list);
      })(),
    );
  });

  await Promise.all(tasks);

  const writeTasks: Array<Promise<void>> = [];
  tileFrames.forEach((frames, baseName) => {
    writeTasks.push(
      (async () => {
        const sorted = [...frames].sort((a, b) => a.frame - b.frame);
        const frameCount = sorted.length;
        const outPngPath = `out-mc/${type}/${baseName}.png`.toLowerCase();

        if (frameCount <= 1) {
          await sharp(sorted[0].buffer).toFile(outPngPath);
          return;
        }

        const sprite = sharp({
          create: {
            width: unitWidth,
            height: unitHeight * frameCount,
            channels: 4,
            background: { r: 0, g: 0, b: 0, alpha: 0 },
          },
        }).composite(
          sorted.map((f, i) => ({
            input: f.buffer,
            top: i * unitHeight,
            left: 0,
          })),
        );

        await sprite.png().toFile(outPngPath);

        const frametime =
          mcAnimationConfig.frameTimeOverrides[baseName] ??
          mcAnimationConfig.defaultFrameTime;
        const mcmetaPath = `${outPngPath}.mcmeta`;
        writeFileSync(
          mcmetaPath,
          JSON.stringify({
            animation: {
              frametime,
            },
          }),
          "utf8",
        );
      })(),
    );
  });

  await Promise.all(writeTasks);
}

processFile(sharpObj.bump, tilesPositionMap, "bump");
processFile(sharpObj.material, tilesPositionMap, "material");
processFile(sharpObj.color, tilesPositionMap, "color");
