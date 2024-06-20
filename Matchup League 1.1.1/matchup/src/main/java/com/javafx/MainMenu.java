package com.javafx;

import java.io.IOException;
import javafx.fxml.FXML;

public class MainMenu extends MenuHandler
{
    @FXML
    private void newSeason() throws IOException {
        App.setRoot("team_select");
    }
}
