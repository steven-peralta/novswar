package com.k9rosie.novswar.database;

import java.util.ArrayList;

public class Table {
    private String name;
    private Database database;
    private ArrayList<Column> columns;

    public Table(String name, Database database) {
        this.name = name;
        this.database = database;
        columns = new ArrayList<Column>();
    }

    public void add(Column column) {
        column.setTable(this);
        columns.add(column);
    }

    public void execute() {
        String prefix = database.getPrefix();
        StringBuilder buffer = new StringBuilder("CREATE TABLE IF NOT EXISTS ");

        if (prefix == null) {
            prefix = "";
        }

        buffer.append(prefix).append(name);
        buffer.append(" ( ");

        for (int i = 0; i < columns.size(); i++) {
            Column column = columns.get(i);

            buffer.append(column.getName()).append(" ");
            buffer.append(column.getType()).append(" ");

            if (column.isPrimary()) {
                buffer.append("PRIMARY KEY ");
            }

            if (column.shouldAutoIncrement() && !database.getType().equalsIgnoreCase("sqlite")) {
                buffer.append("AUTO_INCREMENT ");
            }

            if (!column.getDefaultValue().isEmpty()) {
                buffer.append("DEFAULT ");
                buffer.append(column.getDefaultValue()).append(" ");
            }

            if (i != (columns.size() - 1)) {
                buffer.append(",");
                buffer.append(" ");
            }
        }

        buffer.append(" );");

        database.getDatabaseConnection().scheduleUpdate(buffer.toString());
    }

}
