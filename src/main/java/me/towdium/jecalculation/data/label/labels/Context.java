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

import java.util.Collection;
import java.util.Objects;
import java.util.stream.Stream;

public interface Context<T> {
    LStack<T> create(T t);

    Stream<Pair<TagKey<T>, Stream<T>>> tags();

    Registry<T> registry();

    LTag<T> create(TagKey<T> rl);

    LTag<T> create(TagKey<T> rl, long amount);

    default Collection<TagKey<T>> discover(LStack<T> s) {
        return tags()
                .filter(pair -> pair.getTwo().anyMatch(t -> Objects.equals(t, s.get())))
                .map(Pair::getOne)
                .toList();
    }

    default Stream<LStack<T>> discover(TagKey<T> tag) {
        return tags()
                .filter(pair -> Utilities.equals(pair.getOne(), tag))
                .flatMap(Pair::getTwo)
                .map(this::create);
    }

    default boolean matches(TagKey<?> tag, LStack<?> s) {
        if (s.getContext() != this)
            return false;

        @SuppressWarnings("unchecked")
        Iterable<Holder<T>> tagEntries = registry().getTagOrEmpty((TagKey<T>) tag);
        for (Holder<T> entry : tagEntries) {
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
        public Registry<Item> registry() {
            return BuiltInRegistries.ITEM;
        }

        @Override
        public Stream<Pair<TagKey<Item>, Stream<Item>>> tags() {
            return Utilities.getTags(BuiltInRegistries.ITEM);
        }

        @Override
        public LTag<Item> create(TagKey<Item> rl) {
            return new LItemTag(rl);
        }

        @Override
        public LTag<Item> create(TagKey<Item> rl, long amount) {
            return new LItemTag(rl, amount);
        }
    };
    Context<Fluid> FLUID = new Context<>() {
        @Override
        public LStack<Fluid> create(Fluid fluid) {
            return new LFluidStack(1000, fluid);
        }

        @Override
        public Registry<Fluid> registry() {
            return BuiltInRegistries.FLUID;
        }

        @Override
        public Stream<Pair<TagKey<Fluid>, Stream<Fluid>>> tags() {
            return Utilities.getTags(BuiltInRegistries.FLUID);
        }

        @Override
        public LTag<Fluid> create(TagKey<Fluid> rl) {
            return new LFluidTag(rl);
        }

        @Override
        public LTag<Fluid> create(TagKey<Fluid> rl, long amount) {
            return new LFluidTag(rl, amount);
        }
    };
}
