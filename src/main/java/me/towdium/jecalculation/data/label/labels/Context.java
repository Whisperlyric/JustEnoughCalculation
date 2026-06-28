package me.towdium.jecalculation.data.label.labels;

import me.towdium.jecalculation.utils.Utilities;
import me.towdium.jecalculation.utils.wrappers.Pair;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.material.Fluid;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public interface Context<T> {
    LStack<T> create(T t);

    Stream<Pair<TagKey<@NotNull T>, Stream<T>>> tags();

    Registry<@NotNull T> registry();

    LTag<T> create(TagKey<@NotNull T> rl);

    LTag<T> create(TagKey<@NotNull T> rl, long amount);

    default Collection<TagKey<@NotNull T>> discover(LStack<T> s) {
        return tags()
                .filter(pair -> pair.getTwo().anyMatch(t -> Objects.equals(t, s.get())))
                .map(Pair::getOne)
                .toList();
    }

    default Stream<LStack<T>> discover(TagKey<@NotNull T> tag) {
        return tags()
                .filter(pair -> Utilities.equals(pair.getOne(), tag))
                .flatMap(Pair::getTwo)
                .map(this::create);
    }

    default boolean matches(TagKey<?> tag, LStack<?> s) {
        if (s.getContext() != this)
            return false;

        @SuppressWarnings("unchecked")
        Iterable<Holder<@NotNull T>> tagEntries = registry().getTagOrEmpty((TagKey<@NotNull T>) tag);
        for (Holder<@NotNull T> entry : tagEntries) {
            if (entry.value().equals(s.get())) return true;
        }
        return false;
    }

    Context<Item> ITEM = new Context<>() {
        @Override
        public LStack<Item> create(Item item) {
            return new LItemStack(new ItemStack(item));
        }

        @Override
        public Registry<@NotNull Item> registry() {
            return BuiltInRegistries.ITEM;
        }

        @Override
        public Stream<Pair<TagKey<@NotNull Item>, Stream<@NotNull Item>>> tags() {
            return Utilities.getTags(BuiltInRegistries.ITEM);
        }

        @Override
        public LTag<Item> create(TagKey<@NotNull Item> rl) {
            return new LItemTag(rl);
        }

        @Override
        public LTag<Item> create(TagKey<@NotNull Item> rl, long amount) {
            return new LItemTag(rl, amount);
        }
    };
    Context<Fluid> FLUID = new Context<>() {
        @Override
        public LStack<Fluid> create(Fluid fluid) {
            return new LFluidStack(1000, fluid);
        }

        @Override
        public Registry<@NotNull Fluid> registry() {
            return BuiltInRegistries.FLUID;
        }

        @Override
        public Stream<Pair<TagKey<@NotNull Fluid>, Stream<@NotNull Fluid>>> tags() {
            return Utilities.getTags(BuiltInRegistries.FLUID);
        }

        @Override
        public LTag<Fluid> create(TagKey<@NotNull Fluid> rl) {
            return new LFluidTag(rl);
        }

        @Override
        public LTag<Fluid> create(TagKey<@NotNull Fluid> rl, long amount) {
            return new LFluidTag(rl, amount);
        }
    };
}
