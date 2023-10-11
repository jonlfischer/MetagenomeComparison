package edu.metagenomecomparison.presenter.undoredo;

import javafx.beans.InvalidationListener;
import javafx.beans.binding.Bindings;
import javafx.beans.property.*;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class UndoRedoManager {
    private final ObservableList<Command> undoStack = FXCollections.observableArrayList();
    private final ObservableList<Command> redoStack = FXCollections.observableArrayList();

    private final StringProperty undoLabel = new SimpleStringProperty("Undo");
    private final StringProperty redoLabel = new SimpleStringProperty("Redo");
    private final BooleanProperty canUndo = new SimpleBooleanProperty(false);
    private final BooleanProperty canRedo = new SimpleBooleanProperty(false);

    private final BooleanProperty inUndoRedo = new SimpleBooleanProperty(false);

    public UndoRedoManager() {
        undoStack.addListener((InvalidationListener) e -> undoLabel.set("Undo " + (undoStack.size() == 0 ? "-" : undoStack.get(undoStack.size() - 1).name())));
        redoStack.addListener((InvalidationListener) e -> redoLabel.set("Redo " + (redoStack.size() == 0 ? "-" : redoStack.get(redoStack.size() - 1).name())));
        canUndo.bind(Bindings.size(undoStack).isNotEqualTo(0));
        canRedo.bind(Bindings.size(redoStack).isNotEqualTo(0));
    }

    public void undo() {
        inUndoRedo.set(true);
        try {
            if (isCanUndo()) {
                var command = undoStack.remove(undoStack.size() - 1);
                command.undo();
                if (command.canRedo())
                    redoStack.add(command);
            }
        } finally {
            inUndoRedo.set(false);
        }
    }

    public void redo() {
        inUndoRedo.set(true);
        try {
            if (isCanRedo()) {
                var command = redoStack.remove(redoStack.size() - 1);
                command.redo();
                if (command.canUndo())
                    undoStack.add(command);
            }
        } finally {
            inUndoRedo.set(false);
        }
    }

    public void add(Command command) {
        if (!isInUndoRedo()) {
            if (command.canUndo())
                undoStack.add(command);
            else
                undoStack.clear();
        }
    }

    public void addAndExecute(Command command) {
        if (!isInUndoRedo()) {
            add(command);
            command.redo();
        }
    }


    public void clear() {
        undoStack.clear();
        redoStack.clear();
    }

    public String getUndoLabel() {
        return undoLabel.get();
    }

    public ReadOnlyStringProperty undoLabelProperty() {
        return undoLabel;
    }

    public String getRedoLabel() {
        return redoLabel.get();
    }

    public ReadOnlyStringProperty redoLabelProperty() {
        return redoLabel;
    }

    public boolean isCanUndo() {
        return canUndo.get();
    }

    public ReadOnlyBooleanProperty canUndoProperty() {
        return canUndo;
    }

    public boolean isCanRedo() {
        return canRedo.get();
    }

    public ReadOnlyBooleanProperty canRedoProperty() {
        return canRedo;
    }

    public boolean isInUndoRedo() {
        return inUndoRedo.get();
    }

    public ReadOnlyBooleanProperty inUndoRedoProperty() {
        return inUndoRedo;
    }
}
