package me.towdium.jecalculation.gui.widgets;

public interface IContainer extends IWidget {
    void add(IWidget... w);

    void remove(IWidget... w);

    void clear();
}
