module edu.metagenomenewtest {
    requires javafx.controls;
    requires javafx.fxml;
    requires jloda2;

    opens edu.metagenomecomparison;
    opens edu.metagenomecomparison.presenter.dialogs;
    exports edu.metagenomecomparison;
}