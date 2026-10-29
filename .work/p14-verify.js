// Lane B / Phase 14 验收脚本：rcon 驱动，按场景跑（F-86 ~ F-90）
// 用法：node .work/p14-verify.js <port> <pass> <outfile> <scenario>
// 1.21.1 的玩家 NBT 是 snake_case：active_effects（不是 ActiveEffects），Pos 仍是 [xd, yd, zd]
const net = require('net');
const fs = require('fs');

const PORT = Number(process.argv[2] || 25661);
const PASS = process.argv[3] || 'p14rcon';
const OUTFILE = process.argv[4] || 'D:/project_farm/.work/p14-verify-out.txt';
const SCENARIO = process.argv[5] || 'arena';

const lines = [];
function log(s) { lines.push(s); console.log(s); }
function flush() { fs.writeFileSync(OUTFILE, lines.join('\n') + '\n', 'utf8'); }

function pkt(id, type, body) {
  const b = Buffer.from(body, 'utf8');
  const buf = Buffer.alloc(14 + b.length);
  buf.writeInt32LE(b.length + 10, 0);
  buf.writeInt32LE(id, 4);
  buf.writeInt32LE(type, 8);
  b.copy(buf, 12);
  buf.writeInt16LE(0, 12 + b.length);
  return buf;
}

class Rcon {
  constructor(sock) { this.sock = sock; this.acc = Buffer.alloc(0); this.pending = new Map(); this.id = 2; }
  static connect() {
    return new Promise((resolve, reject) => {
      const sock = net.connect(PORT, '127.0.0.1');
      const r = new Rcon(sock);
      let authed = false;
      sock.on('connect', () => sock.write(pkt(1, 3, PASS)));
      sock.on('error', reject);
      sock.on('data', (d) => {
        r.acc = Buffer.concat([r.acc, d]);
        while (r.acc.length >= 4) {
          const len = r.acc.readInt32LE(0);
          if (r.acc.length < 4 + len) break;
          const id = r.acc.readInt32LE(4);
          const body = r.acc.subarray(12, 4 + len - 2).toString('utf8');
          r.acc = r.acc.subarray(4 + len);
          if (id === 1 && !authed) { authed = true; resolve(r); continue; }
          const p = r.pending.get(id);
          if (p) {
            p.acc += body;
            if (body.length < 4096) { r.pending.delete(id); p.resolve(p.acc); }
          }
        }
      });
      setTimeout(() => reject(new Error('rcon auth timeout')), 15000);
    });
  }
  raw(str) {
    return new Promise((resolve) => {
      const id = this.id++;
      this.pending.set(id, { acc: '', resolve });
      this.sock.write(pkt(id, 2, str));
      setTimeout(() => { if (this.pending.delete(id)) resolve('<TIMEOUT>'); }, 60000);
    });
  }
  close() { this.sock.end(); }
}

let fails = 0;
function assert(cond, label, detail) {
  if (cond) log('  [PASS] ' + label);
  else { fails++; log('  [FAIL] ' + label + (detail ? ' :: ' + detail : '')); }
}

async function cmd(r, c, echo) {
  const body = await r.raw(c);
  if (echo !== false) {
    log('> ' + c);
    for (const line of body.split('\n')) { if (line.trim().length > 0) log('    | ' + line); }
  }
  return body;
}

const sleep = (ms) => new Promise((res) => setTimeout(res, ms));

async function gameTime(r) {
  const body = await r.raw('time query gametime');
  const m = body.match(/(\d+)/);
  return m ? Number(m[1]) : -1;
}

async function waitTicks(r, n) {
  const t0 = await gameTime(r);
  const deadline = Date.now() + n * 50 + 400;
  let t1 = t0;
  while (Date.now() < deadline) {
    await sleep(Math.max(40, Math.min(200, n * 25)));
    t1 = await gameTime(r);
    if (t1 - t0 >= n) break;
  }
  log('  (等待 ' + n + ' 刻：t ' + t0 + ' → ' + t1 + '，实过 ' + (t1 - t0) + ' 刻)');
  return t1;
}

// 读 active_effects：返回 Map<effectId, {amp, dur}>。1.21.1 的每个条目形如
// {duration: 13, show_icon: 0b, "neoforge:cures": [...], amplifier: 0b, id: "farmstorm:zangief_piledriver"}
async function readEffects(r) {
  const body = await r.raw('data get entity @p active_effects');
  const out = new Map();
  const items = body.match(/\{[^{}]*\}/g) || [];
  for (const item of items) {
    const idm = item.match(/id: "([^"]+)"/);
    const am = item.match(/amplifier: (-?\d+)b?/);
    const dm = item.match(/duration: (-?\d+)/);
    // 1.21.1 的 Details.MAP_CODEC 用 optionalFieldOf("amplifier", 0)：amplifier 为 0 时整个字段不写进 NBT
    if (idm) out.set(idm[1], { amp: am ? Number(am[1]) : 0, dur: dm ? Number(dm[1]) : null });
  }
  if (items.length === 0) out.set('<none>', { amp: null, dur: null, raw: body.trim() });
  return out;
}

async function readPos(r) {
  const body = await r.raw('data get entity @p Pos');
  const m = body.match(/\[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/);
  return m ? [Number(m[1]), Number(m[2]), Number(m[3])] : null;
}

function fmteffects(effects) {
  if (effects.has('<none>')) return '无（' + (effects.get('<none>').raw || '').slice(0, 60) + '）';
  const parts = [];
  for (const [id, v] of effects) parts.push(id.replace('farmstorm:', 'fs:').replace('minecraft:', 'mc:') + ' amp=' + v.amp + ' dur=' + v.dur);
  return parts.join('  ');
}

async function reset(r) {
  await cmd(r, 'kill @e[type=item]', false);
  await cmd(r, 'kill @e[type=minecraft:pig]', false);
  await cmd(r, 'gamemode survival @p', false);
  await cmd(r, 'team empty @p', false);
  // 清效果要反复几次：相位推进那一 tick 会把效果重新加回去（硬切换），单次 effect clear 可能被覆盖。
  for (let i = 0; i < 6; i++) {
    await cmd(r, 'effect clear @p', false);
    await sleep(350);
    const eff = await readEffects(r);
    if (eff.size === 0 || eff.has('<none>')) {
      log('  (清场：第 ' + (i + 1) + ' 次 effect clear 后无残留效果)');
      return;
    }
  }
  const eff = await readEffects(r);
  log('  (warn: 清场 6 次后仍有残留效果：' + fmteffects(eff) + ')');
}

async function trapAlive(r, x, y, z) {
  return r.raw(`execute if block ${x} ${y} ${z} farmstorm:zangief_cane`);
}

const SCENARIOS = {
  async arena(r) {
    log('## 测试台与清场基线');
    await cmd(r, 'forceload add 96 96 160 160');
    await cmd(r, 'fill 96 5 96 160 5 160 minecraft:stone');
    await cmd(r, 'fill 96 6 96 160 12 160 minecraft:air');
    await cmd(r, 'gamerule doDaylightCycle false');
    await cmd(r, 'gamerule randomTickSpeed 0');
    await cmd(r, 'gamerule doMobSpawning false');
    await cmd(r, 'gamerule difficulty easy', false);
    await reset(r);
    await cmd(r, 'tp @p 100.5 6.0 100.5');
    await waitTicks(r, 5);
    const items = await cmd(r, 'execute if entity @e[type=item]');
    assert(/Test failed/.test(items), '基线：场上有 0 个掉落物', items.trim());
    const trap = await trapAlive(r, 100, 6, 100);
    assert(/Test failed/.test(trap), '基线：100/6/100 无残留陷阱', trap.trim());
  },

  async 'f86-setblock'(r) {
    log('## F-86 验收 1 · /setblock 返回成功 + 部署方块落地');
    await reset(r);
    await cmd(r, 'fill 96 6 96 160 12 160 minecraft:air', false);
    const res = await cmd(r, 'setblock 100 6 100 farmstorm:zangief_cane');
    assert(/Changed the block/.test(res), 'setblock 返回成功', res.trim());
    const q = await cmd(r, 'execute if block 100 6 100 farmstorm:zangief_cane');
    assert(/Test passed/.test(q), '方块确实是 farmstorm:zangief_cane', q.trim());
    const d = await cmd(r, 'data get block 100 6 100');
    assert(/placedGameTime/.test(d) && /pendingWaves/.test(d), '方块实体已创建（6 个同步字段可读；RT-04：BE 类型与尖牙仙人掌共用）', d.trim().slice(0, 220));
    await cmd(r, 'tp @p 102.5 6.0 100.5');
    await waitTicks(r, 60);
    const alive = await trapAlive(r, 100, 6, 100);
    assert(/Test passed/.test(alive), '2 格外站 60 刻：陷阱仍在（未误触发），同时产出扫描读数', alive.trim());
    await cmd(r, 'setblock 100 6 100 minecraft:air', false);
  },

  // F-87 验收 1 上半：中心距 0.5（站在方块里）
  async 'f87-center'(r) {
    log('## F-87 验收 1 上半 · 中心距 0.5 触发');
    await reset(r);
    await cmd(r, 'fill 96 6 96 160 12 160 minecraft:air', false);
    await cmd(r, 'setblock 100 6 100 farmstorm:zangief_cane', false);
    await cmd(r, 'tp @p 100.5 6.0 100.5');
    // 陷阱是 10 刻一扫，最多要等 10 刻才会触发；轮询到方块消失为止（最多 25 刻）
    let gone = '';
    for (let i = 0; i < 25; i++) {
      gone = await r.raw('execute if block 100 6 100 minecraft:air');
      if (/Test passed/.test(gone)) { log('  (第 ' + (i + 1) + ' 次轮询：方块已消失)'); break; }
      await sleep(50);
    }
    log('> execute if block 100 6 100 minecraft:air');
    log('    | ' + gone.trim());
    assert(/Test passed/.test(gone), '触发后方块消失', gone.trim());
    const effects = await readEffects(r);
    log('    | 效果：' + fmteffects(effects));
    assert(effects.has('farmstorm:zangief_piledriver'), '玩家身上出现 farmstorm:zangief_piledriver', fmteffects(effects));
    const amp = effects.get('farmstorm:zangief_piledriver');
    assert(amp && amp.amp === 0, 'amplifier = 0（相位 0）', fmteffects(effects));
    assert(amp && amp.dur !== null && amp.dur <= 14 && amp.dur >= 10, '相位 0 剩余时长落在 14 刻窗口内（' + (amp ? amp.dur : '?') + '）', fmteffects(effects));
  },

  async 'f87-axis2'(r) {
    log('## F-87 验收 1 下半 · 同轴 2 格不触发');
    await reset(r);
    await cmd(r, 'fill 96 6 96 160 12 160 minecraft:air', false);
    await cmd(r, 'setblock 110 6 110 farmstorm:zangief_cane', false);
    await cmd(r, 'tp @p 112.5 6.0 110.5');
    await waitTicks(r, 40);
    const alive = await trapAlive(r, 110, 6, 110);
    assert(/Test passed/.test(alive), '2 格外（欧氏 2.06）不触发', alive.trim());
    const effects = await readEffects(r);
    assert(!effects.has('farmstorm:zangief_piledriver'), '玩家身上没有 zangief_piledriver（' + fmteffects(effects) + '）');
    await cmd(r, 'setblock 110 6 110 minecraft:air', false);
  },

  async 'f87-diagonal'(r) {
    log('## F-87 验收 2 · 对角 1.4 / 1.4 不触发（D-73 球形口径）');
    await reset(r);
    await cmd(r, 'fill 96 6 96 160 12 160 minecraft:air', false);
    await cmd(r, 'setblock 120 6 120 farmstorm:zangief_cane', false);
    await cmd(r, 'tp @p 121.9 6.0 121.9');
    await waitTicks(r, 40);
    const alive = await trapAlive(r, 120, 6, 120);
    assert(/Test passed/.test(alive), '对角 1.4/1.4（欧氏 2.04，切比雪夫 1 会误判）不触发', alive.trim());
    const effects = await readEffects(r);
    assert(!effects.has('farmstorm:zangief_piledriver'), '玩家身上没有 zangief_piledriver（' + fmteffects(effects) + '）');
    await cmd(r, 'setblock 120 6 120 minecraft:air', false);
    // 对照：相邻方块中心（欧氏 1.12）必须触发
    await cmd(r, 'setblock 130 6 130 farmstorm:zangief_cane', false);
    await cmd(r, 'tp @p 131.5 6.0 130.5');
    await waitTicks(r, 12);
    const gone = await cmd(r, 'execute if block 130 6 130 minecraft:air');
    assert(/Test passed/.test(gone), '对照：相邻方块中心（欧氏 1.12）触发', gone.trim());
  },

  async 'f87-creative'(r) {
    log('## F-87 验收 3 之一 · 创造模式不触发（D-71）');
    await reset(r);
    await cmd(r, 'fill 96 6 96 160 12 160 minecraft:air', false);
    await cmd(r, 'gamemode creative @p', false);
    await cmd(r, 'setblock 140 6 140 farmstorm:zangief_cane', false);
    await cmd(r, 'tp @p 140.5 6.0 140.5');
    await waitTicks(r, 40);
    const alive = await trapAlive(r, 140, 6, 140);
    assert(/Test passed/.test(alive), '创造模式玩家站在方块里不触发', alive.trim());
    await cmd(r, 'setblock 140 6 140 minecraft:air', false);
    await cmd(r, 'gamemode survival @p', false);
  },

  async 'f87-spectator'(r) {
    log('## F-87 验收 3 之二 · 旁观者不触发（D-71）');
    await reset(r);
    await cmd(r, 'fill 96 6 96 160 12 160 minecraft:air', false);
    await cmd(r, 'gamemode spectator @p', false);
    await cmd(r, 'setblock 145 6 145 farmstorm:zangief_cane', false);
    await cmd(r, 'tp @p 145.5 6.0 145.5');
    await waitTicks(r, 40);
    const alive = await trapAlive(r, 145, 6, 145);
    assert(/Test passed/.test(alive), '旁观者站在方块里不触发', alive.trim());
    await cmd(r, 'setblock 145 6 145 minecraft:air', false);
    await cmd(r, 'gamemode survival @p', false);
  },

  async 'f87-ally'(r) {
    log('## F-87 验收 3 之三 · 同队玩家不触发（D-71 / C-9 实时解析）');
    await reset(r);
    await cmd(r, 'fill 96 6 96 160 12 160 minecraft:air', false);
    await cmd(r, 'team remove red', false);
    await cmd(r, 'team remove blue', false);
    await cmd(r, 'team add red', false);
    await cmd(r, 'setblock 150 6 150 farmstorm:zangief_cane', false);
    await cmd(r, 'data merge block 150 6 150 {ownerUuid:[I;1,2,3,4],ownerName:"LaneAOwner"}');
    const readback = await cmd(r, 'data get block 150 6 150 ownerName');
    assert(/LaneAOwner/.test(readback), '归属已写入方块实体', readback.trim());
    await cmd(r, 'team join red LaneAOwner', false);
    await cmd(r, 'team join red @p', false);
    await cmd(r, 'tp @p 150.5 6.0 150.5');
    await waitTicks(r, 40);
    const alive = await trapAlive(r, 150, 6, 150);
    assert(/Test passed/.test(alive), '同队玩家站在方块里不触发（归属者与目标同在 red）', alive.trim());
    // 反向对照：归属者改到 blue，目标仍在 red ⇒ 应触发
    await cmd(r, 'team add blue', false);
    await cmd(r, 'team join blue LaneAOwner', false);
    await waitTicks(r, 20);
    const gone = await cmd(r, 'execute if block 150 6 150 minecraft:air');
    assert(/Test passed/.test(gone), '反向对照：归属者换到 blue（与目标不同队）后触发', gone.trim());
    await cmd(r, 'team remove red', false);
    await cmd(r, 'team remove blue', false);
    await cmd(r, 'setblock 150 6 150 minecraft:air', false);
  },

  async 'f87-mob'(r) {
    log('## F-87 验收 3 之四 · 生物不触发（只认玩家）');
    await reset(r);
    await cmd(r, 'fill 96 6 96 160 12 160 minecraft:air', false);
    await cmd(r, 'setblock 155 6 155 farmstorm:zangief_cane', false);
    await cmd(r, 'summon minecraft:pig 155.5 6.0 155.5');
    await cmd(r, 'tp @p 158.5 6.0 155.5');
    await waitTicks(r, 40);
    const alive = await trapAlive(r, 155, 6, 155);
    assert(/Test passed/.test(alive), '生物站在方块里不触发', alive.trim());
    await cmd(r, 'setblock 155 6 155 minecraft:air', false);
    await cmd(r, 'kill @e[type=minecraft:pig]', false);
  },

  async 'f87-nodrop'(r) {
    log('## F-87 验收 4 · 触发不自毁掉落（A-08）');
    await reset(r);
    await cmd(r, 'fill 96 6 96 160 12 160 minecraft:air', false);
    await cmd(r, 'setblock 100 6 100 farmstorm:zangief_cane', false);
    const before = await cmd(r, 'execute if entity @e[type=item]');
    log('  (触发前掉落物：' + before.trim() + ')');
    await cmd(r, 'tp @p 100.5 6.0 100.5');
    await waitTicks(r, 15);
    const gone = await cmd(r, 'execute if block 100 6 100 minecraft:air');
    assert(/Test passed/.test(gone), '触发后方块消失', gone.trim());
    const after = await cmd(r, 'execute if entity @e[type=item]');
    assert(/Test failed/.test(after), '触发后掉落物 = 0', after.trim());
  },

  // 相位时间轴：一次触发，逐 ~1 刻采样 Pos + active_effects
  async timeline(r, cfg) {
    log('## 相位时间轴 · 陷阱 (' + cfg.x + ',' + cfg.y + ',' + cfg.z + ')');
    await reset(r);
    await cmd(r, 'fill 96 6 96 160 12 160 minecraft:air', false);
    // 陷阱必须站在实体支撑上：平地直接用 y=5 的台面，两格 / 四格高台各砌一根柱子
    if (cfg.y > 6) {
      // 高台按「平台」搭 3×3 顶面（文档的「两格 / 四格高台」），而不是 1×1 石柱：
      // 1×1 石柱下，漂浮段的横向漂移会把玩家挪出柱顶那一列，伺服量的地面就变成远处台面，落地自然提前。
      await cmd(r, `fill ${cfg.x - 1} 6 ${cfg.z - 1} ${cfg.x + 1} ${cfg.y - 1} ${cfg.z + 1} minecraft:stone`);
    }
    await cmd(r, `setblock ${cfg.x} ${cfg.y} ${cfg.z} farmstorm:zangief_cane`);
    const placed = await r.raw(`execute if block ${cfg.x} ${cfg.y} ${cfg.z} farmstorm:zangief_cane`);
    log('  (陷阱放置核对：' + placed.trim() + ')');
    await cmd(r, `tp @p ${cfg.x + 0.5} ${cfg.y}.0 ${cfg.z + 0.5}`);
    const t0 = await gameTime(r);
    let sawPiledriver = false, sawLevit = false, lastAmp = null;
    for (let i = 0; i < cfg.samples; i++) {
      const t = await gameTime(r);
      const effects = await readEffects(r);
      const pos = await readPos(r);
      const pd = effects.get('farmstorm:zangief_piledriver');
      const lv = effects.get('minecraft:levitation');
      if (pd) sawPiledriver = true;
      if (lv) sawLevit = true;
      lastAmp = pd ? pd.amp : lastAmp;
      log('  t=' + String(t - t0).padStart(3) + ' (' + t + ')  Y=' + (pos ? pos[1].toFixed(3) : '?') +
          '  piledriver=' + (pd ? 'amp=' + pd.amp + ' dur=' + pd.dur : '无') +
          '  levitation=' + (lv ? 'amp=' + lv.amp + ' dur=' + lv.dur : '无'));
      await sleep(cfg.interval || 40);
    }
    assert(sawPiledriver, '时间轴里出现过 farmstorm:zangief_piledriver');
    assert(sawLevit, '时间轴里出现过 minecraft:levitation（相位 1）');
    const alive = await trapAlive(r, cfg.x, cfg.y, cfg.z);
    assert(/Test failed/.test(alive), '采样结束时陷阱方块已消失', alive.trim());
  },

  async 'timeline-flat'(r) { await SCENARIOS.timeline(r, { x: 100, y: 6, z: 100, samples: 110 }); },
  async 'timeline-2high'(r) { await SCENARIOS.timeline(r, { x: 110, y: 8, z: 110, samples: 110 }); },
  async 'timeline-4high'(r) { await SCENARIOS.timeline(r, { x: 120, y: 10, z: 120, samples: 110 }); },

  // 三档地形各跑一次相位 2 中 /kill：参数由场景名决定
  async killInPhase2(r, cfg) {
    log('## F-90 验收 4 · 相位 2 中 /kill @p 后重生（地形 ' + cfg.label + '）');
    await reset(r);
    await cmd(r, 'gamerule doImmediateRespawn true', false);
    await cmd(r, 'fill 96 6 96 160 12 160 minecraft:air', false);
    if (cfg.y > 6) {
      await cmd(r, `fill ${cfg.x - 1} 6 ${cfg.z - 1} ${cfg.x + 1} ${cfg.y - 1} ${cfg.z + 1} minecraft:stone`, false);
    }
    const placed = await cmd(r, `setblock ${cfg.x} ${cfg.y} ${cfg.z} farmstorm:zangief_cane`);
    log('  (放置：' + placed.trim() + ')');
    await cmd(r, `tp @p ${cfg.x + 0.5} ${cfg.y}.0 ${cfg.z + 0.5}`);
    // 轮询到相位 2（amplifier 2）为止，最多 60 刻
    let before = null;
    for (let i = 0; i < 60; i++) {
      const eff = await readEffects(r);
      const pd = eff.get('farmstorm:zangief_piledriver');
      if (pd && pd.amp === 2) { before = eff; log('  (第 ' + (i + 1) + ' 次轮询：已进入相位 2)'); break; }
      await sleep(50);
    }
    assert(before !== null, 'kill 前已进入相位 2（amplifier 2）', before ? fmteffects(before) : '未读到 amp=2');
    log('  (kill 前效果：' + (before ? fmteffects(before) : '无') + ')');
    await cmd(r, 'kill @p');
    await waitTicks(r, 12);
    const effects = await readEffects(r);
    assert(!effects.has('farmstorm:zangief_piledriver'), '重生后无 zangief_piledriver（' + fmteffects(effects) + '）');
    assert(!effects.has('minecraft:levitation'), '重生后无 levitation（' + fmteffects(effects) + '）');
    const motion = await cmd(r, 'data get entity @p Motion');
    const m = motion.match(/\[(-?[\d.]+)d, (-?[\d.]+)d, (-?[\d.]+)d\]/);
    assert(m !== null && Math.abs(Number(m[1])) < 0.01 && Math.abs(Number(m[3])) < 0.01, '重生后水平速度 ≈ 0', motion.trim());
    // noGravity 必须已被恢复：重生后抬到 +6 格自由落体，6 刻内必须明显下坠
    const beforeLift = await readPos(r);
    const liftX = beforeLift ? beforeLift[0] : 100.5;
    const liftZ = beforeLift ? beforeLift[2] : 100.5;
    const liftY = (beforeLift ? beforeLift[1] : 6) + 6;
    await cmd(r, `tp @p ${liftX} ${liftY} ${liftZ}`, false);
    await waitTicks(r, 6);
    const after = await readPos(r);
    assert(after !== null && after[1] < liftY - 0.3, '重生后重力已恢复（' + liftY.toFixed(1) + ' → ' + (after ? after[1].toFixed(2) : '?') + '）', JSON.stringify(after));
    await cmd(r, 'gamerule doImmediateRespawn false', false);
  },

  async 'f90-kill'(r) {
    await SCENARIOS.killInPhase2(r, { label: '平地', x: 100, y: 6, z: 100 });
  },
  async 'f90-kill-2high'(r) {
    await SCENARIOS.killInPhase2(r, { label: '两格高台', x: 110, y: 8, z: 110 });
  },
  async 'f90-kill-4high'(r) {
    await SCENARIOS.killInPhase2(r, { label: '四格高台', x: 120, y: 10, z: 120 });
  },
};

(async () => {
  let r;
  try {
    r = await Rcon.connect();
    log('# Lane B / Phase 14 验收 · 场景 ' + SCENARIO + ' · ' + new Date().toISOString());
    const fn = SCENARIOS[SCENARIO];
    if (!fn) { log('未知场景：' + SCENARIO + '；可用：' + Object.keys(SCENARIOS).join(', ')); r.close(); flush(); process.exit(2); }
    await fn(r);
    log('');
    log(fails === 0 ? '== 本场景 PASS ==' : '== 本场景 FAIL ' + fails + ' 项 ==');
    r.close();
    flush();
    process.exit(fails === 0 ? 0 : 1);
  } catch (e) {
    log('!! 异常：' + (e && e.message ? e.message : String(e)));
    if (r) r.close();
    flush();
    process.exit(3);
  }
})();
