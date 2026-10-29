# F-98 证据 · 三个新进度

HEAD 之后的工作区（未 commit）｜改动 **1 个源文件 + 3 个新 JSON**

## 0. 改动
- `datagen/ModAdvancementProvider.java`：`generate()` 末尾加 13/14/15 三条 `impossible()` 进度；`AWARD_REQUIRED` 追加同样三个 id。
- `src/generated/resources/data/farmstorm/advancement/{first_molotov,zangief_hit,first_wild_hunter}.json`：由 `runData` 生成（**没手写**）。

## 1. 三条 JSON 的静态属性（脚本 `.work/f98-check.js`）
| id | parent | icon | 显示名（zh） | criteria 键 |
|---|---|---|---|---|
| `first_molotov` | **`farmstorm:first_throw`** ✔ | `farmstorm:molotov` | 火了！ / 初次投出莫洛托夫！ | `first_molotov` |
| `zangief_hit` | **`farmstorm:first_deploy`** ✔ | `farmstorm:zangief_cane` | 手摇重 360 / 桑吉尔蔗首次命中一名玩家 | `zangief_hit` |
| `first_wild_hunter` | **`farmstorm:root`** ✔ | `farmstorm:wild_apple` | 狂野猎人 / 首次获得「狂 野 猎 人」效果 | `first_wild_hunter` |

### `frame = task` / `show_toast = true` 为什么 JSON 里看不到这两个字段（**不是缺陷**）
1.21.1 的 `DisplayInfo` codec（`DisplayInfo.java`）：
```
AdvancementType.CODEC.optionalFieldOf("frame", AdvancementType.TASK)
Codec.BOOL.optionalFieldOf("show_toast", Boolean.valueOf(true))
Codec.BOOL.optionalFieldOf("announce_to_chat", Boolean.valueOf(true))
```
`frame` 与 `show_toast` 的默认值**正好**是 `task` / `true`，所以取默认值时**整个字段不写进 JSON**；
而 `announce_to_chat` 默认是 true、我们传 false ⇒ 它**必须**写出来。三个文件里都恰好只有 `"announce_to_chat": false`，
这同时反证了「缺省字段 = 取默认值」的语义 ⇒ **frame = task、show_toast = true 成立**。
（既有 12 条进度也是同样的写法，本仓一致。）

## 2. 总数与「既有 12 条不动」
```
advancement json 总数: 15
git status --porcelain -- src/generated/resources/data/farmstorm/advancement
  ?? first_molotov.json / ?? zangief_hit.json / ?? first_wild_hunter.json      ← 只有这三条是新增
git diff --stat -- …/advancement   → 空
⇒ 既有 12 条 JSON 一个字节都没动
```

## 3. runData 幂等 + 语言键
```
第一次  HashCache: total files 190, old 187, new 191, removed stale 0, written: 3   ← 三条新 JSON
第二次  HashCache: total files 190, old 190, new 191, removed stale 0, written: 0   ← 幂等
```
语言键（F-97 已先立，本次**没动** `ModLangProvider`）：
```
advancements.farmstorm.first_molotov.title      zh="火了！"        en="It's Lit!"
advancements.farmstorm.first_molotov.description zh="初次投出莫洛托夫！" en="Throw a Molotov for the first time"
advancements.farmstorm.zangief_hit.title        zh="手摇重 360"     en="Hand-Cranked 360"
advancements.farmstorm.zangief_hit.description  zh="桑吉尔蔗首次命中一名玩家" en="Land a Zangief Cane on a player for the first time"
advancements.farmstorm.first_wild_hunter.title  zh="狂野猎人"       en="Wild Hunter"
advancements.farmstorm.first_wild_hunter.description zh="首次获得「狂 野 猎 人」效果" en="Obtain the Wild Hunter effect for the first time"
```
核对方式：脚本从每个 advancement JSON 里读出 `display.title/description.translate`，再到两份 lang 里查——
**15 条进度、30 个标题/描述键，zh_cn 与 en_us 全部命中，缺键数 0**。
键名也逐字对齐（`advancements.farmstorm.<id>.title|.description`，id 与 JSON 文件名、criteria 键、`AWARD_REQUIRED` 三处一致）。

## 4. 尚未接线的部分（**本任务边界外**，需要你派活）
三条都是 `minecraft:impossible` 触发器，**必须由服务端 `FarmstormAdvancements.award(player, "<id>")` 授予**。
当前 `AWARD_REQUIRED` 已登记它们，但代码里还没有对应的 award 调用点：

| id | 触发点应该在哪 | 归谁 |
|---|---|---|
| `first_molotov` | 投掷物实体生成时（`entity/thrown/MolotovProjectile.java`，F-69 的文件） | 该文件归属需你确认 |
| `zangief_hit` | `behavior/impl/ZangiefCaneTrap.trigger()` 里，施加 amplifier 0 之后（**本 lane 的文件**，与 F-87 同源） | 我可以顺手加 |
| `first_wild_hunter` | 玩家首次获得 `farmstorm:wild_hunter` 效果的判定点（`effect/WildHunterEffect` 或 `event/`；Lane A 的文件） | Lane A |

没接线时这三条进度**只能靠 `/advancement grant` 手动给**，不会自己弹 toast —— 所以「动态验收」除了要客户端，还依赖上面三处接线。
要不要我现在就把 `zangief_hit` 那一处接上（只改 `ZangiefCaneTrap.trigger` 一个方法体，加一行 award）？另外两处等你定归属。
