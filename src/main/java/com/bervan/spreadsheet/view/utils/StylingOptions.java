package com.bervan.spreadsheet.view.utils;
//
//import com.bervan.common.BervanTextField;
//import com.bervan.spreadsheet.model.SpreadsheetCell;
//import com.vaadin.flow.component.button.Button;
//import com.vaadin.flow.component.contextmenu.MenuItem;
//import com.vaadin.flow.component.contextmenu.SubMenu;
//import com.vaadin.flow.component.dialog.Dialog;
//import com.vaadin.flow.component.notification.Notification;
//import com.vaadin.flow.component.notification.NotificationVariant;
//import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
//
//import java.util.Set;
//import java.util.function.Function;
//
public class StylingOptions extends VerticalLayout {
//    private MenuItem stylingMenu;
//    private MenuItem boldMenuItem;
//    private MenuItem italicMenuItem;
//    private MenuItem underlineMenuItem;
//    private MenuItem linkMenuItem;
//    private MenuItem imageMenuItem;
//
//    private void applyStyle(String style, Set<SpreadsheetCell> selectedCells, SpreadsheetCell focusedCell, Function<Void, Void> refreshTable) {
//        String openTag = "";
//        String closeTag = "";
//        switch (style) {
//            case "bold" -> {
//                openTag = "<b>";
//                closeTag = "</b>";
//            }
//            case "italic" -> {
//                openTag = "<i>";
//                closeTag = "</i>";
//            }
//            case "underline" -> {
//                openTag = "<u>";
//                closeTag = "</u>";
//            }
//            default -> {
//                showErrorNotification("Unknown style: " + style);
//                return;
//            }
//        }
//
//        if (selectedCells.size() != 0) {
//            for (SpreadsheetCell selectedCell : selectedCells) {
//                String content = selectedCell.getHtmlContent() != null ? selectedCell.getHtmlContent() : "";
//                // Avoid duplicating tags if they already exist
//                if (!content.contains(openTag)) {
//                    selectedCell.setHtmlContent(openTag + content + closeTag);
//                }
//            }
//
//        } else {
//            if (focusedCell == null) {
//                showErrorNotification("No cell is focused or selected!");
//                return;
//            }
//
//            String content = focusedCell.getHtmlContent() != null ? focusedCell.getHtmlContent() : "";
//            // Avoid duplicating tags if they already exist
//            if (!content.contains(openTag)) {
//                focusedCell.setHtmlContent(openTag + content + closeTag);
//            }
//        }
//
//        refreshTable.apply(null);
//        showSuccessNotification("Style applied to focused (selected) cell(s).");
//    }
//
//    public void stylingMenuOptions(MenuItem stylingMenu, Set<SpreadsheetCell> selectedCells, SpreadsheetCell focusedCell, Function<Void, Void> refreshTable) {
//        SubMenu stylingSubMenu = stylingMenu.getSubMenu();
//
//        boldMenuItem = stylingSubMenu.addItem("Bold", event -> {
//            applyStyle("bold", selectedCells, focusedCell, refreshTable);
//        });
//
//        italicMenuItem = stylingSubMenu.addItem("Italic", event -> {
//            applyStyle("italic", selectedCells, focusedCell, refreshTable);
//        });
//
//        underlineMenuItem = stylingSubMenu.addItem("Underline", event -> {
//            applyStyle("underline", selectedCells, focusedCell, refreshTable);
//        });
//
//        linkMenuItem = stylingSubMenu.addItem("Add Link", event -> {
//            applyLink(selectedCells, focusedCell, refreshTable);
//        });
//
//        imageMenuItem = stylingSubMenu.addItem("Insert Image", event -> {
//            insertImage(selectedCells, focusedCell, refreshTable);
//        });
//    }
//
//    private SpreadsheetCell getSelectedOrFocusedCell(Set<SpreadsheetCell> selectedCells, SpreadsheetCell focusedCell) {
//        if (focusedCell == null && selectedCells.size() == 0) {
//            showErrorNotification("No cell is focused (selected).");
//            return null;
//        }
//
//        if (selectedCells.size() > 1) {
//            showErrorNotification("More than one cell selected!");
//            return null;
//        }
//
//        if (selectedCells.size() == 1) {
//            return selectedCells.iterator().next();
//        } else {
//            return focusedCell;
//        }
//    }
//
//
//    private void applyLink(Set<SpreadsheetCell> selectedCells, SpreadsheetCell focusedCell, Function<Void, Void> refreshTable) {
//        focusedCell = getSelectedOrFocusedCell(selectedCells, focusedCell);
//        if (focusedCell == null) {
//            return;
//        }
//
//        // Show a dialog to get the URL
//        Dialog dialog = new Dialog();
//        dialog.setWidth("400px");
//
//        BervanTextField urlField = new BervanTextField("URL", "https://example.com");
//        SpreadsheetCell finalFocusedCell = focusedCell;
//        Button okButton = new Button("OK", e -> {
//            String url = urlField.getValue();
//            if (url == null || url.isEmpty()) {
//                showErrorNotification("URL cannot be empty.");
//                return;
//            }
//            String content = finalFocusedCell.getHtmlContent() != null ? finalFocusedCell.getHtmlContent() : finalFocusedCell.getValue();
//            finalFocusedCell.setHtmlContent("<a href=\"" + url + "\">" + content + "</a>");
//            refreshTable.apply(null);
//            showSuccessNotification("Link applied to focused cell.");
//            dialog.close();
//        });
//        Button cancelButton = new Button("Cancel", e -> dialog.close());
//        HorizontalLayout buttons = new HorizontalLayout(okButton, cancelButton);
//        VerticalLayout layout = new VerticalLayout(urlField, buttons);
//        dialog.add(layout);
//        dialog.open();
//    }
//
//    private void insertImage(Set<SpreadsheetCell> selectedCells, SpreadsheetCell focusedCell, Function<Void, Void> refreshTable) {
//        focusedCell = getSelectedOrFocusedCell(selectedCells, focusedCell);
//
//        if (focusedCell == null) {
//            return;
//        }
//
//        // Show a dialog to get the image URL
//        Dialog dialog = new Dialog();
//        dialog.setWidth("400px");
//
//        BervanTextField urlField = new BervanTextField("Image URL", "https://example.com/image.png");
//        SpreadsheetCell finalFocusedCell = focusedCell;
//        Button okButton = new Button("OK", e -> {
//            String url = urlField.getValue();
//            if (url == null || url.isEmpty()) {
//                showErrorNotification("Image URL cannot be empty.");
//                return;
//            }
//            finalFocusedCell.setHtmlContent("<img src=\"" + url + "\" alt=\"Image\" />");
//            finalFocusedCell.setValue("");
//            refreshTable.apply(null);
//            showSuccessNotification("Image inserted into focused cell.");
//            dialog.close();
//        });
//        Button cancelButton = new Button("Cancel", e -> dialog.close());
//        HorizontalLayout buttons = new HorizontalLayout(okButton, cancelButton);
//        VerticalLayout layout = new VerticalLayout(urlField, buttons);
//        dialog.add(layout);
//        dialog.open();
//    }
//
//    public void showErrorNotification(String msg) {
//        Notification notification = Notification.show(msg);
//        notification.addThemeVariants(NotificationVariant.LUMO_ERROR);
//    }
//
//    public void showSuccessNotification(String msg) {
//        Notification notification = Notification.show(msg);
//        notification.addThemeVariants(NotificationVariant.LUMO_SUCCESS);
//    }
}
