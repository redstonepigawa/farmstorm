package com.dashpig.farmstorm.client;

import com.dashpig.farmstorm.Farmstorm;
import com.dashpig.farmstorm.config.FarmstormClientConfig;
import com.dashpig.farmstorm.network.CoreListPayload;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.RenderGuiEvent;

/**
 * 核心浮窗（D-143 / 30 号 §2.4.4）：一行一个**自己 + 队友**的核心，含未加载区块与其它维度。
 *
 * <p>四条前置早退（顺序即优先级）：无玩家 / 开关关 → {@code hideGui} 且非拖拽 → 有别的 Screen
 * 且不是 {@link CoreHudDragScreen} → **0 核不画**。第 2、3 条在拖拽态失效：玩家按 J 进来就是要看见它。
 *
 * <p>版式**以 A-41 为准**：行高 9 / 行距 1 / 内边距 2，面板高 = 10n + 3 ⇒ 8 行 83 px、9 行 93 px。
 * A-34 的 93 px 是它的等价推论（8 行核心 + 1 行溢出），两套分解不并存。
 */
@EventBusSubscriber(modid = Farmstorm.MOD_ID, value = Dist.CLIENT)
public final class CoreHudRenderer {
    /** 行高（{@code Font#lineHeight} = 9）。 */
    public static final int LINE_HEIGHT = 9;
    /** 行距。 */
    public static final int LINE_GAP = 1;
    /** 内边距（上下左右各 2 px；1 px 边框画在这 2 px 之内）。 */
    public static final int PADDING = 2;
    /** 面板高 = 10n + 3 的那个 +3（上下各 2 px 内边距，末行不留行距）。 */
    public static final int PANEL_EXTRA_HEIGHT = 3;
    /** 核心行上限：8 行核心（A-34）。 */
    public static final int MAX_CORE_LINES = 8;

    private static final int COLOR_BACKGROUND = 0x80000000;
    private static final int COLOR_BORDER = 0xFF000000;
    private static final int COLOR_SELF = 0xFF55FF55;
    private static final int COLOR_ALLY = 0xFFFFFFFF;
    private static final int COLOR_OVERFLOW = 0xFFAAAAAA;

    /** 「自己」标识（唯一为此新增的语言键；维度标识取维度 id 的 path，不新增键）。 */
    private static final String SELF_LABEL_KEY = "gui.farmstorm.core_hud.self";

    private CoreHudRenderer() {}

    @SubscribeEvent
    public static void onRenderGuiPost(RenderGuiEvent.Post event) {
        Minecraft minecraft = Minecraft.getInstance();
        boolean editing = CoreHudDragScreen.active();
        if (minecraft.player == null || !FarmstormClientConfig.hudEnabled.get()) {
            return;                                        // 无玩家 / 开关关
        }
        if (minecraft.options.hideGui && !editing) {
            return;                                        // F1 的 hideGui 优先级最高，拖拽态例外
        }
        if (minecraft.screen != null && !(minecraft.screen instanceof CoreHudDragScreen)) {
            return;                                        // 只有拖拽 Screen 自己例外
        }
        List<CoreListPayload.Core> cores = ClientCoreList.cores();
        if (cores.isEmpty()) {
            return;                                        // 0 核不画（拆卸掉唯一核心后整块面板消失）
        }
        List<String> rows = buildRows(cores, ClientCoreList.total());
        GuiGraphics graphics = event.getGuiGraphics();
        Font font = minecraft.font;
        int width = panelWidth(font, rows);
        int height = panelHeight(rows.size());
        int originX = clampX(editing ? CoreHudDragScreen.previewX() : FarmstormClientConfig.hudX.get(),
                width, graphics.guiWidth());
        int originY = clampY(editing ? CoreHudDragScreen.previewY() : FarmstormClientConfig.hudY.get(),
                height, graphics.guiHeight());
        graphics.fill(originX, originY, originX + width, originY + height, COLOR_BORDER);
        graphics.fill(originX + 1, originY + 1, originX + width - 1, originY + height - 1, COLOR_BACKGROUND);
        int coreLines = Math.min(cores.size(), MAX_CORE_LINES);
        for (int i = 0; i < rows.size(); i++) {
            int color = i < coreLines
                    ? (cores.get(i).self() ? COLOR_SELF : COLOR_ALLY)
                    : COLOR_OVERFLOW;
            graphics.drawString(font, rows.get(i),
                    originX + PADDING, originY + PADDING + i * (LINE_HEIGHT + LINE_GAP), color, false);
        }
    }

    /**
     * 行文本：前 8 行是核心，第 9 行（若 total &gt; 8）是 {@code "+" + (total - 8)}。
     *
     * <p>序号是**按截断前全量排序**的 1 基序号，所以 {@code +N} 出现时编号仍然连续不断；
     * {@code +N} 是纯数字字符串拼接，**不新增语言键**（30 号 §5.5 第 6 条）。
     */
    private static List<String> buildRows(List<CoreListPayload.Core> cores, int total) {
        int shown = Math.min(cores.size(), MAX_CORE_LINES);
        List<String> rows = new ArrayList<>(shown + 1);
        String selfLabel = null;
        for (int i = 0; i < shown; i++) {
            CoreListPayload.Core core = cores.get(i);
            String who;
            if (core.self()) {
                if (selfLabel == null) {
                    selfLabel = Component.translatable(SELF_LABEL_KEY).getString();
                }
                who = selfLabel;
            } else {
                who = core.ownerName();
            }
            rows.add(String.format(Locale.ROOT, "#%d [%s] (%d, %d, %d) %s",
                    i + 1, core.dimension().getPath(),
                    core.pos().getX(), core.pos().getY(), core.pos().getZ(), who));
        }
        if (total > MAX_CORE_LINES) {
            rows.add("+" + (total - MAX_CORE_LINES));
        }
        return rows;
    }

    /** 面板宽 = 最长行实测宽 + 左右各 2 px（逐帧算，不缓存）。 */
    private static int panelWidth(Font font, List<String> rows) {
        int widest = 0;
        for (String row : rows) {
            widest = Math.max(widest, font.width(row));
        }
        return widest + 2 * PADDING;
    }

    /** 面板高 = 10n + 3（n 行：行高 9 + 行距 1 共 n-1 份，加内边距 4）。 */
    public static int panelHeight(int lines) {
        return lines * (LINE_HEIGHT + LINE_GAP) + PANEL_EXTRA_HEIGHT;
    }

    /** 当前应绘制的面板尺寸 {@code [宽, 高]}；拖拽 Screen 的命中判定与钳制都用这一份算式。 */
    public static int[] panelSize(Font font) {
        List<String> rows = buildRows(ClientCoreList.cores(), ClientCoreList.total());
        return new int[] {panelWidth(font, rows), panelHeight(rows.size())};
    }

    /** 水平钳制到 {@code [0, guiWidth - 宽]}：窗口拉伸 / GUI 缩放后旧坐标可能越界。 */
    public static int clampX(int x, int width, int screenWidth) {
        return Math.max(0, Math.min(x, Math.max(0, screenWidth - width)));
    }

    /** 垂直钳制到 {@code [0, guiHeight - 高]}。 */
    public static int clampY(int y, int height, int screenHeight) {
        return Math.max(0, Math.min(y, Math.max(0, screenHeight - height)));
    }
}
