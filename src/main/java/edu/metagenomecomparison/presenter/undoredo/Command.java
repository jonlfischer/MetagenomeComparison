package edu.metagenomecomparison.presenter.undoredo;

public interface Command {
    void undo();

    void redo();

    String name();

    boolean canUndo();

    boolean canRedo();
}
