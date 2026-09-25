module com.student.movieapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.google.gson;

    opens com.student.movieapp to javafx.graphics;
    opens com.student.movieapp.controller to javafx.fxml;
    opens com.student.movieapp.model to javafx.base, com.google.gson;

    exports com.student.movieapp;
    exports com.student.movieapp.model;
    exports com.student.movieapp.controller;
    exports com.student.movieapp.dao;
}
