package me.towdium.jecalculation.data;

import me.towdium.jecalculation.JustEnoughCalculation;
import me.towdium.jecalculation.data.structure.*;
import me.towdium.jecalculation.network.packets.PCalculator;
import me.towdium.jecalculation.utils.Utilities;
import me.towdium.jecalculation.utils.wrappers.Pair;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import me.towdium.jecalculation.annotation.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class Controller {
    public static final String KEY_MATH = "math";
    public static final String KEY_CRAFT = "craft";
    public static final String KEY_PLAYER = "player";

    static RecordPlayer rPlayerClient;
    static RecordCraft rCraftClient;
    static RecordMath rMathClient;

    static boolean serverActive = false;
    static RecordPlayer rPlayerServer;

    public static void setRecordsServer(RecordPlayer r) {
        rPlayerServer = r;
        serverActive = true;
    }

    public static boolean isServerActive() {
        return serverActive;
    }

    static Recipes getRecipes() {
        if (isServerActive()) return rPlayerServer.recipes;
        else return rPlayerClient.recipes;
    }

    public static List<Pair<String, Recipes>> discover() {
        File dir = new File(FabricLoader.getInstance().getConfigDir().toFile(), JustEnoughCalculation.MODID + "/data");
        File[] fs = dir.listFiles();
        Function<File, Recipes> read = f -> {
            CompoundTag nbt = Utilities.Json.read(f);
            if (nbt == null) {
                JustEnoughCalculation.logger.warn("File {} contains invalid records.", f.getAbsolutePath());
                return null;
            }
            return new Recipes(nbt);
        };
        if (fs == null) return new ArrayList<>();
        return Arrays.stream(fs)
                .map(i -> new Pair<>(i.getName(), read.apply(i)))
                .filter(i -> i.two != null)
                .collect(Collectors.toList());
    }

    public static void inport(Recipes recipes, String group) {
        ArrayList<Recipe> buffer = new ArrayList<>();
        recipes.getGroup(group).stream().filter(i -> !hasDuplicate(i)).forEach(buffer::add);
        for (Recipe r : buffer) addRecipe(group, r);
    }

    private static void export(String s, Function<Recipes, CompoundTag> r) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        File f = new File(FabricLoader.getInstance().getConfigDir().toFile(), JustEnoughCalculation.MODID + "/data/" + s + ".json");
        Utilities.Json.write(r.apply(getRecipes()), f);
        player.sendSystemMessage(Component.translatable(
                "jecalculation.chat.export", f.getAbsolutePath()));
    }

    public static void export(String group) {
        export(group, i -> i.serialize(Collections.singleton(group)));
    }

    public static void export() {
        export("groups", Recipes::serialize);
    }

    @Nullable
    public static String getLast() {
        if (isServerActive()) return rPlayerServer.last;
        return rPlayerClient.last;
    }

    static void setLast(String last) {
        if (isServerActive()) rPlayerServer.last = last;
        else rPlayerClient.last = last;
    }

    public static List<String> getGroups() {
        return getRecipes().getGroups();
    }

    public static void setRecipe(String neu, String old, int index, Recipe recipe) {
        getRecipes().set(neu, old, index, recipe);
        setLast(neu);
    }

    public static void renameGroup(String old, String neu) {
        getRecipes().renameGroup(old, neu);
        setLast(neu);
    }

    public static void addRecipe(String group, Recipe recipe) {
        getRecipes().add(group, recipe);
        setLast(group);
    }

    public static void setRecipe(String group, int index, Recipe recipe) {
        getRecipes().set(group, index, recipe);
        setLast(group);
    }

    public static void removeRecipe(String group, int index) {
        getRecipes().remove(group, index);
        setLast(group);
    }

    public static void removeGroup(String group) {
        getRecipes().remove(group);
        setLast(group);
    }

    public static Recipe getRecipe(String group, int index) {
        return getRecipes().getRecipe(group, index);
    }

    public static Stream<Pair<String, List<Recipe>>> stream() {
        return getRecipes().stream();
    }

    public static Recipes.RecipeIterator recipeIterator() {
        return getRecipes().recipeIterator();
    }

    public static Recipes.RecipeIterator recipeIterator(String group) {
        return getRecipes().recipeIterator(group);
    }

    public static void setRMath(RecordMath math, @Nullable ItemStack is, int slot) {
        setR(math, i -> rMathClient = i, KEY_MATH, is, slot);
    }

    public static void setRCraft(RecordCraft rc, @Nullable ItemStack is, int slot) {
        setR(rc, i -> rCraftClient = i, KEY_CRAFT, is, slot);
    }

    public static RecordCraft getRCraft(@Nullable ItemStack is) {
        return getR(rCraftClient, KEY_CRAFT, RecordCraft::new, is);
    }

    public static RecordMath getRMath(@Nullable ItemStack is) {
        return getR(rMathClient, KEY_MATH, RecordMath::new, is);
    }

    private static <T extends IRecord> void setR(T t, Consumer<T> c, String s, @Nullable ItemStack is, int slot) {
        if (!isServerActive()) c.accept(t);
        else if (is != null) {
            Utilities.getTag(is).put(s, t.serialize());
            ClientPlayNetworking.send(new PCalculator(is, slot));
        }
    }

    public static <T> T getR(T t, String s, Function<CompoundTag, T> f, @Nullable ItemStack is) {
        if (!isServerActive()) return t;
        else if (is != null) return f.apply(Utilities.getTag(is).getCompoundOrEmpty(s));
        else throw new RuntimeException("Internal error");
    }

    public static void loadFromLocal() {
        rPlayerClient = new RecordPlayer();
        rCraftClient = new RecordCraft(new CompoundTag());
        rMathClient = new RecordMath(new CompoundTag());

        File configFile = new File(Utilities.config(), "recipes.json");
        if (configFile.exists()) {
            CompoundTag nbt = Utilities.Json.read(configFile);
            if (nbt != null) {
                rPlayerClient = new RecordPlayer(nbt);
            }
        }
    }

    public static void saveToLocal() {
        if (rPlayerClient == null) return;
        File configDir = Utilities.config();
        if (!configDir.mkdirs() && !configDir.exists()) {
            JustEnoughCalculation.logger.warn("Failed to create config directory: {}", configDir);
        }
        File configFile = new File(Utilities.config(), "recipes.json");
        Utilities.Json.write(rPlayerClient.serialize(), configFile);
    }

    public static class Server {
        public static void onJoin(net.minecraft.server.level.ServerPlayer player) {
            // Send record to client
        }
    }

    public static class Client {
        public static void onLogOut(@Nullable net.minecraft.client.player.LocalPlayer player) {
            serverActive = false;
            rPlayerServer = null;
            saveToLocal();
        }
    }

    public static boolean hasDuplicate(Recipe r) {
        return recipeIterator().stream().anyMatch(i -> i.equals(r));
    }
}
