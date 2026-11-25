package cl.doraking.mod.trade;

import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.ClickType;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

/**
 * Menu para editar los tradeos (cofre de 6 filas). Cada fila tiene 2 tradeos:
 * [Pago 1][Pago 2][->][Producto] | [Pago 1][Pago 2][->][Producto]. Maximo 10 en el menu.
 * Las casillas son "fantasma": al hacer clic con un item en la mano se COPIA (no se gasta),
 * asi funciona con items de cualquier mod, con su NBT incluido.
 */
public class TradeEditorMenu extends ChestMenu {
    public static final int EDITOR_TRADES = 10;
    private static final int SLOT_INFO = 45, SLOT_SAVE = 49, SLOT_RESET = 53;
    private final Container container;

    public TradeEditorMenu(int id, Inventory inv, Container container) {
        super(MenuType.GENERIC_9x6, id, inv, container, 6);
        this.container = container;
    }

    // ---------- abrir ----------
    public static void open(ServerPlayer player) {
        SimpleContainer c = new SimpleContainer(54);
        fill(c, TradeConfig.get());
        player.openMenu(new SimpleMenuProvider((id, inv, p) -> new TradeEditorMenu(id, inv, c),
                Component.literal("Tradeos del Dueño de Doraking")));
    }

    private static int base(int trade) { return (trade / 2) * 9 + (trade % 2) * 5; }

    private static ItemStack named(net.minecraft.world.item.Item item, String name, String... lore) {
        ItemStack s = new ItemStack(item);
        s.setHoverName(Component.literal(name).withStyle(ChatFormatting.YELLOW));
        if (lore.length > 0) {
            CompoundTag display = s.getOrCreateTagElement("display");
            ListTag l = new ListTag();
            for (String line : lore)
                l.add(StringTag.valueOf(Component.Serializer.toJson(Component.literal(line).withStyle(ChatFormatting.GRAY))));
            display.put("Lore", l);
        }
        return s;
    }

    private static void fill(SimpleContainer c, List<TradeConfig.Trade> trades) {
        c.clearContent();
        for (int r = 0; r < 5; r++) {
            c.setItem(r * 9 + 2, named(Items.LIME_STAINED_GLASS_PANE, "→ da"));
            c.setItem(r * 9 + 7, named(Items.LIME_STAINED_GLASS_PANE, "→ da"));
            c.setItem(r * 9 + 4, named(Items.GRAY_STAINED_GLASS_PANE, " "));
        }
        for (int i = 45; i < 54; i++) c.setItem(i, named(Items.GRAY_STAINED_GLASS_PANE, " "));
        c.setItem(SLOT_INFO, named(Items.PAPER, "Cómo se usa",
                "Cada tradeo: [Pago 1] [Pago 2 opcional] → [Producto].",
                "Con un item en la mano, clic en una casilla: se COPIA ahí (no se gasta).",
                "Funciona con items de cualquier mod.",
                "Mano vacía: clic izquierdo = +1 cantidad, clic derecho = -1 (0 = borrar).",
                "Shift+clic en una casilla = borrarla.",
                "Se guarda solo al cerrar. Máximo 10 tradeos aquí (el archivo admite más)."));
        c.setItem(SLOT_SAVE, named(Items.EMERALD_BLOCK, "Guardar y cerrar"));
        c.setItem(SLOT_RESET, named(Items.TNT, "Restaurar tradeos originales", "Vuelve a los 8 tradeos por defecto."));
        for (int t = 0; t < Math.min(EDITOR_TRADES, trades.size()); t++) {
            TradeConfig.Trade tr = trades.get(t);
            int b = base(t);
            c.setItem(b, tr.a.copy());
            c.setItem(b + 1, tr.b.copy());
            c.setItem(b + 3, tr.result.copy());
        }
    }

    private static boolean isTradeSlot(int slot) {
        if (slot >= 45) return false;
        int col = slot % 9;
        return col == 0 || col == 1 || col == 3 || col == 5 || col == 6 || col == 8;
    }

    // ---------- clics ----------
    @Override
    public void clicked(int slotId, int button, ClickType type, Player player) {
        if (slotId >= 0 && slotId < 54) {
            if (isTradeSlot(slotId)) ghostClick(slotId, button, type, player);
            else if (slotId == SLOT_SAVE) player.closeContainer();
            else if (slotId == SLOT_RESET) fill((SimpleContainer) container, TradeConfig.defaults());
            return;
        }
        if (type == ClickType.QUICK_MOVE || type == ClickType.PICKUP_ALL || type == ClickType.QUICK_CRAFT) return;
        super.clicked(slotId, button, type, player);
    }

    private void ghostClick(int slot, int button, ClickType type, Player player) {
        ItemStack cur = container.getItem(slot);
        if (type == ClickType.CLONE) return;
        if (type == ClickType.QUICK_MOVE) { container.setItem(slot, ItemStack.EMPTY); return; }
        if (type == ClickType.SWAP) {
            if (button >= 0 && button < 9) {
                ItemStack hb = player.getInventory().getItem(button);
                container.setItem(slot, hb.isEmpty() ? ItemStack.EMPTY : hb.copy());
            }
            return;
        }
        if (type != ClickType.PICKUP) return;
        ItemStack carried = getCarried();
        if (!carried.isEmpty()) {
            container.setItem(slot, carried.copy());
        } else if (!cur.isEmpty()) {
            int n = cur.getCount() + (button == 0 ? 1 : -1);
            if (n <= 0) container.setItem(slot, ItemStack.EMPTY);
            else { ItemStack c = cur.copy(); c.setCount(Math.min(n, Math.max(1, c.getMaxStackSize()))); container.setItem(slot, c); }
        }
    }

    @Override
    public boolean canDragTo(Slot slot) { return false; }

    @Override
    public ItemStack quickMoveStack(Player player, int index) { return ItemStack.EMPTY; }

    // ---------- guardar ----------
    @Override
    public void removed(Player player) {
        super.removed(player);
        if (player.level().isClientSide) return;
        List<TradeConfig.Trade> old = TradeConfig.get();
        List<TradeConfig.Trade> out = new ArrayList<>();
        for (int t = 0; t < EDITOR_TRADES; t++) {
            int b = base(t);
            ItemStack a = container.getItem(b), b2 = container.getItem(b + 1), r = container.getItem(b + 3);
            if (a.isEmpty() && !b2.isEmpty()) { a = b2; b2 = ItemStack.EMPTY; }
            if (a.isEmpty() || r.isEmpty()) continue;
            TradeConfig.Trade tr = new TradeConfig.Trade(a.copy(), b2.copy(), r.copy());
            if (t < old.size()) tr.maxUses = old.get(t).maxUses;
            out.add(tr);
        }
        for (int i = EDITOR_TRADES; i < old.size(); i++) out.add(old.get(i));   // conserva los extras del archivo
        TradeConfig.set(out);
        player.displayClientMessage(Component.literal("Tradeos guardados (" + out.size() + "). Archivo: config/doraking_trades.json"), false);
    }
}
