package com.test;

import com.menu.App;
import javafx.scene.text.Font;

public class StyleTest {
    public boolean FontTest() {
        Font agency = Font.loadFont(App.class.getResourceAsStream("/css/fonts/agency_fb.ttf"), 40);
        Font brit = Font.loadFont(App.class.getResourceAsStream("/css/fonts/britannic_bold.ttf"), 40);
        if (agency == null && brit == null) return false;
        System.out.println(agency.getName() + "\n" + brit.getName());
        return true;
    }
}
