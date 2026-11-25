package cl.doraking.mod.trade;

import cl.doraking.mod.ModItems;
import com.google.gson.*;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.fml.loading.FMLPaths;
import net.minecraftforge.registries.ForgeRegistries;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

/**
 * Tradeos del Dueno guardados en config/doraking_trades.json.
 * Usan ids de item ("modid:item"), asi que sirven con items de cualquier mod (y con NBT).
 */
public class TradeConfig {
    private static final Logger LOGGER = LogManager.getLogger("doraking");
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().disableHtmlEscaping().create();

    public static class Trade {
        public ItemStack a = ItemStack.EMPTY;      // pago 1
        public ItemStack b = ItemStack.EMPTY;      // pago 2 (opcional)
        public ItemStack result = ItemStack.EMPTY; // producto
        public int maxUses = 9999;

        public Trade() { }
        public Trade(ItemStack a, ItemStack b, ItemStack result) {
            this.a = a; this.b = b; this.result = result;
        }
        public boolean isValid() { return !a.isEmpty() && !result.isEmpty(); }
    }

    private static List<Trade> trades = new ArrayList<>();
    private static boolean loaded = false;
    /** Sube cada vez que cambian los tradeos; el Dueno lo usa para refrescar sus ofertas. */
    public static volatile int version = 0;

    private static Path file() {
        return FMLPaths.CONFIGDIR.get().resolve("doraking_trades.json");
    }

    public static synchronized List<Trade> get() {
        if (!loaded) load();
        return trades;
    }

    public static synchronized void set(List<Trade> newTrades) {
        trades = new ArrayList<>(newTrades);
        loaded = true;
        version++;
        save();
    }

    public static List<Trade> defaults() {
        List<Trade> l = new ArrayList<>();
        Item[] sabores = { ModItems.DORAKIN_ANKO.get(), ModItems.DORAKIN_MANJAR.get(),
                ModItems.DORAKIN_PASTELERA.get(), ModItems.DORAKIN_NUTELLA.get() };
        for (Item s : sabores) {
            l.add(new Trade(new ItemStack(Items.EMERALD, 2), ItemStack.EMPTY, new ItemStack(s, 1)));
            l.add(new Trade(new ItemStack(Items.EMERALD, 5), ItemStack.EMPTY, new ItemStack(s, 3)));
        }
        return l;
    }

    public static synchronized void load() {
        Path f = file();
        if (!Files.exists(f)) {
            trades = defaults();
            loaded = true;
            version++;
            save();
            return;
        }
        List<Trade> list = new ArrayList<>();
        try (Reader r = Files.newBufferedReader(f)) {
            JsonArray arr = JsonParser.parseReader(r).getAsJsonObject().getAsJsonArray("trades");
            for (JsonElement el : arr) {
                try {
                    JsonObject o = el.getAsJsonObject();
                    Trade t = new Trade();
                    t.a = parseStack(o.get("cost1"));
                    t.b = o.has("cost2") ? parseStack(o.get("cost2")) : ItemStack.EMPTY;
                    t.result = parseStack(o.get("result"));
                    if (o.has("maxUses")) t.maxUses = Math.max(1, o.get("maxUses").getAsInt());
                    if (t.isValid()) list.add(t);
                    else LOGGER.warn("[Doraking] Tradeo ignorado (item desconocido o vacio): {}", el);
                } catch (Exception e) {
                    LOGGER.warn("[Doraking] Tradeo con formato invalido ignorado: {}", el);
                }
            }
            trades = list;
        } catch (Exception e) {
            LOGGER.error("[Doraking] No se pudo leer doraking_trades.json, se usan los tradeos por defecto", e);
            trades = defaults();
        }
        loaded = true;
        version++;
    }

    private static synchronized void save() {
        JsonObject root = new JsonObject();
        root.addProperty("_ayuda", "Cada tradeo: cost1 (pago), cost2 (pago opcional), result (producto), maxUses. "
                + "item = id de cualquier mod (modid:item), count = cantidad, nbt = datos opcionales en SNBT. "
                + "Tambien puedes editarlos en el juego: agachate y clic derecho al Dueno, o /doraking tradeos. "
                + "Tras editar a mano usa /doraking recargar.");
        JsonArray arr = new JsonArray();
        for (Trade t : trades) {
            JsonObject o = new JsonObject();
            o.add("cost1", stackToJson(t.a));
            if (!t.b.isEmpty()) o.add("cost2", stackToJson(t.b));
            o.add("result", stackToJson(t.result));
            o.addProperty("maxUses", t.maxUses);
            arr.add(o);
        }
        root.add("trades", arr);
        try (Writer w = Files.newBufferedWriter(file())) {
            GSON.toJson(root, w);
        } catch (Exception e) {
            LOGGER.error("[Doraking] No se pudo guardar doraking_trades.json", e);
        }
    }

    private static JsonObject stackToJson(ItemStack s) {
        JsonObject o = new JsonObject();
        ResourceLocation id = ForgeRegistries.ITEMS.getKey(s.getItem());
        o.addProperty("item", id == null ? "minecraft:air" : id.toString());
        o.addProperty("count", s.getCount());
        if (s.hasTag()) o.addProperty("nbt", s.getTag().toString());
        return o;
    }

    private static ItemStack parseStack(JsonElement el) throws Exception {
        JsonObject o = el.getAsJsonObject();
        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(o.get("item").getAsString()));
        if (item == null || item == Items.AIR) return ItemStack.EMPTY;
        int count = o.has("count") ? Math.max(1, o.get("count").getAsInt()) : 1;
        ItemStack s = new ItemStack(item, count);
        if (o.has("nbt")) {
            CompoundTag tag = TagParser.parseTag(o.get("nbt").getAsString());
            s.setTag(tag);
        }
        return s;
    }
}
