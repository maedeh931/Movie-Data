module com.student.movieapp {
    requires javafx.controls;
    requires javafx.fxml;
    requires java.sql;
    requires com.fasterxml.jackson.databind;

    opens com.student.movieapp to javafx.graphics;
    opens com.student.movieapp.controller to javafx.fxml;
    opens com.student.movieapp.model to javafx.base, com.fasterxml.jackson.databind;
    opens com.student.movieapp.dao to com.fasterxml.jackson.databind;

    exports com.student.movieapp;
    exports com.student.movieapp.model;
    exports com.student.movieapp.controller;
    exports com.student.movieapp.dao;
}
