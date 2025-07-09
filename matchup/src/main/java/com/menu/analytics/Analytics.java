package com.menu.analytics;

import com.menu.App;
import com.menu.Menu;
import com.repo.Repository;
import com.util.Debug;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.chart.PieChart.Data;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;

/**
 * Page that allows user to view various metrics on the entities in the database.
 * @since 1.2.1
 */
public class Analytics extends Menu {

    @FXML Button backButton;
    @FXML Label totFLabel, totTLabel, totLgLabel;
    @FXML PieChart typeFreqChart, strFreqChart, wkFreqChart;
    @FXML VBox typeFreqList, strFreqList, wkFreqList;

    //private Map<String, String> filtersMap;
    private Repository repo;

    @FXML private void initialize() {
        Debug.write("Analytics.initialize");
        repo = App.getRepo();
        fighterAnalyze();
    }

    /**
     * Fills analytics page with appropriate values.
     * @return exit code
     * @since 1.2.1
     */
    private int fighterAnalyze() {
        Debug.write("Analytics.fighterAnalyze");
        //total fighters
        final long totalFighters = repo.totalFighterCount();
        final long totalTeams = repo.totalTeamCount();
        final long totalLeagues = repo.totalLeagueCount();

        totFLabel.setText(Long.toString(totalFighters));
        totTLabel.setText(Long.toString(totalTeams));
        totLgLabel.setText(Long.toString(totalLeagues));

        fillTypeChart(typeFreqChart, "types");
        fillTypeChart(strFreqChart, "strType");
        fillTypeChart(strFreqChart, "wkType");

        //double avg = fDAO.avg("strVal", "strType LIKE '%" + type + "%'");
        //double avg = -1.0 * (fDAO.avg("wkVal", "wkType LIKE '%" + type + "%'")); 
        return 1;
    }

    /**
     * Populates a pie chart with the frequency of types.
     * @param chart to populate
     * @param column to measure frequency of
     * @return true if successful, false if not
     * @since 1.2.1
     * @see Type
     */
    private void fillTypeChart(PieChart chart, String column) {
        Debug.write("Analytics.fillTypeChart");
        ObservableList<Data> data = FXCollections.observableArrayList();
        //TODO: sort
        for (Type type : App.allTypes()) {
            double freq = repo.typeFreq(column, type.getChar());
            data.add(new Data(Character.toString(type.getChar()), freq));
            //TODO: next patch
            if (column.equals("types")) {
                Label label = new Label(type.getChar() + ": " + (Double.toString(freq).substring(0, 4)));
                typeFreqList.getChildren().add(label);
            }
        }
        typeFreqChart.setData(data);
    }

    @FXML private void analyticsGoBack() {write("Analytics.goBack"); App.goBack();}
} 
