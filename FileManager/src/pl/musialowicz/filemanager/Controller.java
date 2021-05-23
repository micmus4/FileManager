package pl.musialowicz.filemanager;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.SortedList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.control.*;
import javafx.scene.input.*;
import javafx.scene.layout.BorderPane;
import javafx.util.Callback;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.*;
import java.nio.file.attribute.BasicFileAttributes;
import java.nio.file.attribute.FileTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class Controller {

    private ObservableList<String> fileList1, fileList2;

    @FXML
    private BorderPane mainWindow;

    private final String usersHomeDir = System.getProperty("user.home");

    private Path leftAreaPath = Paths.get(usersHomeDir);
    private Path rightAreaPath = Paths.get(usersHomeDir);

    @FXML
    private ListView<String> listView1, listView2;

    @FXML
    private RadioButton sortByNameLeftSideDown, sortByDateLeftSideDown,
                        sortByNameLeftSideUp, sortByDateLeftSideUp,
                        sortByNameRightSideDown, sortByDateRightSideDown,
                        sortByNameRightSideUp, sortByDateRightSideUp;

    @FXML
    private Label leftPathLabel, rightPathLabel;

    public void initialize() {
        setLabels();
        getFiles();
        listView1.setItems(fileList1);
        listView2.setItems(fileList2);
        leftAreaCellFactory();
        rightAreaCellFactory();
        leftSortBy();
        rightSortBy();
    }


    /***
     * Loads files from user home path to left/right area directory.
     * @author Michal Musialowicz
     */

    public void getFiles(){
        File folder1 = new File(leftAreaPath.toString());
        File folder2 = new File(rightAreaPath.toString());
        File[] listOfFiles1 = folder1.listFiles();
        File[] listOfFiles2 = folder2.listFiles();
        List<String> files1 = new ArrayList<>();
        List<String> files2 = new ArrayList<>();
        files1.add("[...]");
        files2.add("[...]");
        loadFiles(listOfFiles1, files1, leftAreaPath);
        loadFiles(listOfFiles2, files2, rightAreaPath);
        fileList1 = FXCollections.observableList(files1);
        fileList2 = FXCollections.observableList(files2);
    }

    /***
     * Handles all actions in right area directory.
     *
     * @author Michal Musialowicz
     * @see #copyDir(String, int)
     * @see #copyFile(String, int)
     * @see #loadFiles(File[], List, Path)
     * @see #leftSortBy()
     * @see #rightSortBy()
     */


    public void leftAreaCellFactory(){
        listView1.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                ListCell<String> listCell = new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item == null){
                            setText("");
                            setGraphic(null);
                        } else if (item.equals("[...]")){
                            setText("[...]");
                        } else {
                            String[] items = item.split(";;;");
                            setText(items[0] + items[1]);
                        }
                    }
                };

               listCell.setOnMouseClicked(mouseEvent -> {
                   if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                       if(mouseEvent.getClickCount() == 2) {
                           if (listCell.getItem() == null) {
                               return;
                           } else if (listCell.getItem().equals("[...]")){
                               char[] characters = leftAreaPath.toString().toCharArray();
                               int howManyBackSlaches = 0;
                               int counter = 0;
                               for(char character : characters){
                                   String ch = String.valueOf(character);
                                   if(ch.equals("\\")){
                                       howManyBackSlaches++;
                                   }
                               }
                               StringBuilder sb = new StringBuilder();
                               for(int i = 0; i < leftAreaPath.toString().length(); i++){
                                   if(String.valueOf(characters[i]).equals("\\")){
                                       counter++;
                                       if(counter == howManyBackSlaches){
                                           break;
                                       }
                                   }
                                   sb.append(characters[i]);

                               }
                               leftAreaPath = Paths.get(sb.toString());
                               File folder1 = new File(leftAreaPath.toString());
                               File[] listOfFiles1 = folder1.listFiles();
                               List<String> files1 = new ArrayList<>();
                               files1.add("[...]");
                               loadFiles(listOfFiles1, files1, leftAreaPath);
                               fileList1 = FXCollections.observableArrayList(files1);
                               listView1.setItems(null);
                               listView1.setItems(fileList1);
                               leftPathLabel.setText("Path: " + leftAreaPath.toString());
                               listView1.refresh();
                           } else if (listCell.getItem().contains("<DIR>")) {
                                   String[] items = listCell.getItem().split(" ;;;");
                                   String item = items[0];
                                   leftAreaPath = Paths.get(leftAreaPath.toString() + "\\" + item);
                                   File folder1 = new File(leftAreaPath.toString());
                                   File[] listOfFiles1 = folder1.listFiles();
                                   List<String> files1 = new ArrayList<>();
                                   files1.add("[...]");
                                   loadFiles(listOfFiles1, files1, leftAreaPath);
                                   fileList1 = FXCollections.observableArrayList(files1);
                                   listView1.setItems(null);
                                   listView1.setItems(fileList1);
                                  // leftSortBy();
                                   leftPathLabel.setText("Path: " + leftAreaPath.toString());
                                   listView1.refresh();
                           }
                       }
                   }
               });

                listCell.setOnDragDetected((MouseEvent event) -> {
                    Dragboard db = listCell.startDragAndDrop(TransferMode.COPY_OR_MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(listCell.getItem());
                    db.setContent(content);
                    event.consume();
                });

                listCell.setOnDragEntered((DragEvent event) -> listCell.setStyle("-fx-background-color: aqua;"));

                listCell.setOnDragExited((DragEvent event) -> listCell.setStyle(""));

                listCell.setOnDragOver((DragEvent event) -> {
                    Dragboard db = event.getDragboard();
                    if (db.hasString()) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    }
                    event.consume();
                });

                listCell.setOnDragDropped((DragEvent event) -> {
                    Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasString()) {
                        String[] items = db.getString().split(" ;;;");
                        String item = items[0];
                        for(String file : fileList1){
                            String[] tempItems = file.split(" ;;;");
                            String tempItem = tempItems[0];
                            if(item.equals(tempItem)){
                                return;
                            }
                        }
                    fileList1.add(db.getString());
                    if(db.getString().contains("<DIR>")){
                        copyDir(db.getString(), 2);
                    } else {
                        copyFile(db.getString(), 2);
                    }
                    leftSortBy();
                    rightSortBy();
                    listView1.refresh();
                    success = true;
                    }
                    event.setDropCompleted(success);
                    event.consume();
                });

                return listCell;
            }
        });
    }


    /***
     * Handles all actions in right area directory.
     *
     * @author Michal Musialowicz
     * @see #copyDir(String, int)
     * @see #copyFile(String, int)
     * @see #loadFiles(File[], List, Path)
     * @see #leftSortBy()
     * @see #rightSortBy()
     */


    public void rightAreaCellFactory(){
        listView2.setCellFactory(new Callback<>() {
            @Override
            public ListCell<String> call(ListView<String> param) {
                ListCell<String> listCell = new ListCell<>() {
                    @Override
                    protected void updateItem(String item, boolean empty) {
                        super.updateItem(item, empty);
                        if(item == null){
                            setText("");
                            setGraphic(null);
                        } else if (item.equals("[...]")){
                            setText("[...]");
                        } else {
                            String[] items = item.split(";;;");
                            setText(items[0] + items[1]);
                        }
                    }
                };


                listCell.setOnMouseClicked(mouseEvent -> {
                    if(mouseEvent.getButton().equals(MouseButton.PRIMARY)){
                        if(mouseEvent.getClickCount() == 2) {
                            if (listCell.getItem() == null) {
                                return;
                            } else if (listCell.getItem().equals("[...]")){
                                char[] characters = rightAreaPath.toString().toCharArray();
                                int howManyBackSlaches = 0;
                                int counter = 0;
                                for(char character : characters){
                                    String ch = String.valueOf(character);
                                    if(ch.equals("\\")){
                                        howManyBackSlaches++;
                                    }
                                }
                                StringBuilder sb = new StringBuilder();
                                for(int i = 0; i < rightAreaPath.toString().length(); i++){
                                    if(String.valueOf(characters[i]).equals("\\")){
                                        counter++;
                                        if(counter == howManyBackSlaches){
                                            break;
                                        }
                                    }
                                    sb.append(characters[i]);

                                }
                                rightAreaPath = Paths.get(sb.toString());
                                File folder2 = new File(rightAreaPath.toString());
                                File[] listOfFiles2 = folder2.listFiles();
                                List<String> files2 = new ArrayList<>();
                                files2.add("[...]");
                                loadFiles(listOfFiles2, files2, rightAreaPath);
                                fileList2 = FXCollections.observableArrayList(files2);
                                listView2.setItems(null);
                                listView2.setItems(fileList2);
                                rightPathLabel.setText("Path: " + rightAreaPath.toString());
                                listView2.refresh();
                            } else if (listCell.getItem().contains("<DIR>")){
                                String[] items = listCell.getItem().split(" ;;;");
                                String item = items[0];
                                rightAreaPath = Paths.get(rightAreaPath.toString() + "\\" + item);
                                File folder2 = new File(rightAreaPath.toString());
                                if(!folder2.exists()){
                                    return;
                                }
                                File[] listOfFiles2 = folder2.listFiles();
                                List<String> files2 = new ArrayList<>();
                                files2.add("[...]");
                                loadFiles(listOfFiles2, files2, rightAreaPath);
                                fileList2 = FXCollections.observableArrayList(files2);
                                listView2.setItems(null);
                                listView2.setItems(fileList2);
                                // leftSortBy();
                                rightPathLabel.setText("Path: " + rightAreaPath.toString());
                                listView2.refresh();
                            }
                        }
                    }
                });


                listCell.setOnDragDetected((MouseEvent event) -> {
                    Dragboard db = listCell.startDragAndDrop(TransferMode.COPY_OR_MOVE);
                    ClipboardContent content = new ClipboardContent();
                    content.putString(listCell.getItem());
                    db.setContent(content);
                    event.consume();
                });

                listCell.setOnDragEntered((DragEvent event) -> listCell.setStyle("-fx-background-color: aqua;"));

                listCell.setOnDragExited((DragEvent event) -> listCell.setStyle(""));

                listCell.setOnDragOver((DragEvent event) -> {
                    Dragboard db = event.getDragboard();
                    if (db.hasString()) {
                        event.acceptTransferModes(TransferMode.COPY_OR_MOVE);
                    }
                    event.consume();
                });

                listCell.setOnDragDropped((DragEvent event) -> {
                    Dragboard db = event.getDragboard();
                    boolean success = false;
                    if (db.hasString()) {
                        String[] items = db.getString().split(" ;;;");
                        String item = items[0];
                        for(String file : fileList2){
                            String[] tempItems = file.split(" ;;;");
                            String tempItem = tempItems[0];
                            if(item.equals(tempItem)){
                                return;
                            }
                        }
                        fileList2.add(db.getString());
                        if(db.getString().contains("<DIR>")){
                            copyDir(db.getString(), 1);
                        } else {
                            copyFile(db.getString(), 1);
                        }
                        leftSortBy();
                        rightSortBy();
                        listView2.refresh();
                        success = true;

                    }
                    event.setDropCompleted(success);
                    event.consume();
                });

                return listCell;
            }
        });
    }



    /***
     *
     * Sorts files/directories by name, either ascending or descending - based on the 'order' param.
     *
     * @author Michal Musialowicz
     * @param list - observableArrayList which is given as parameter to newly created SortedList
     * @param listView - listView to be later given sorted items.
     * @param order - order, 1 for descending, -1 for ascending
     */
    public void sortListByName(ObservableList<String> list, ListView<String> listView, int order){
        SortedList<String> sortedList = new SortedList<>(list, (str1, str2) -> {
            if(str1.equals("[...]")){
                return -1;
            } else if (str2.equals("[...]")){
                return 1;
            } else {
               return str1.compareToIgnoreCase(str2) * order;
            }
        });

        listView.setItems(sortedList);
        list.setAll(sortedList);
    }


    /***
     *
     * Sorts files/directories by date/time, either ascending or descending - based on the 'order' param.
     *
     * @author Michal Musialowicz
     * @param list - observableArrayList which is given as parameter to newly created SortedList
     * @param listView - listView to be later given sorted items.
     * @param path - path to left side directory
     * @param anotherPath - path to right side directory
     * @param order - order, 1 for descending, -1 for ascending
     */


    public void sortListByDate(ObservableList<String> list, ListView<String> listView, Path path, Path anotherPath, int order){
        SortedList<String> sortedList = new SortedList<>(list, (str1, str2) -> {
            if(str1.equals("[...]")){
                return -1;
            } else if (str2.equals("[...]")){
                return 1;
            } else {
                String stringPath = path.toString();
                String[] splittedStr1 = str1.split(";;;");
                String[] splittedStr2 = str2.split(";;;");
                String modStr1 = splittedStr1[0].trim();
                String modStr2 = splittedStr2[0].trim();
                Path path1 = Paths.get(stringPath + "\\" + modStr1);
                Path path2 = Paths.get(stringPath + "\\" + modStr2);
                BasicFileAttributes attr1 = null;
                BasicFileAttributes attr2 = null;
                try {
                    attr1 = Files.readAttributes(path1, BasicFileAttributes.class);
                } catch (IOException e){
                    path1 = Paths.get(anotherPath.toString() + "\\" + modStr1);
                    try {
                        attr1 = Files.readAttributes(path1, BasicFileAttributes.class);
                    } catch (IOException eve){
                        System.out.println("...");
                    }
                }
                try {
                    attr2 = Files.readAttributes(path2, BasicFileAttributes.class);
                } catch (IOException e){
                    path2 = Paths.get(anotherPath.toString() + "\\" + modStr2);
                    try {
                        attr2 = Files.readAttributes(path2, BasicFileAttributes.class);
                    } catch (IOException eve){
                        System.out.println("...");
                    }
                }
                FileTime time1 = attr1.creationTime();
                FileTime time2 = attr2.creationTime();
                return -time1.compareTo(time2) * order;
            }
        });

        listView.setItems(sortedList);
        list.setAll(sortedList);
    }



    /***
     *
     * Sorts left area directory based on selected RadioButton.
     *
     * @author Michal Musialowicz
     * @see #sortListByDate(ObservableList, ListView, Path, Path, int)
     * @see #sortListByName(ObservableList, ListView, int)
     */


    public void leftSortBy(){
        if(sortByDateLeftSideDown.isSelected()){
            sortListByDate(fileList1, listView1, leftAreaPath, rightAreaPath, 1);
        }
        if(sortByNameLeftSideDown.isSelected()){
            sortListByName(fileList1, listView1, 1);
        }
        if(sortByDateLeftSideUp.isSelected()){
            sortListByDate(fileList1, listView1, leftAreaPath, rightAreaPath, -1);
        }
        if(sortByNameLeftSideUp.isSelected()){
            sortListByName(fileList1, listView1, -1);
        }
    }


    /***
     *
     * Sorts right area directory based on selected RadioButton.
     *
     * @author Michal Musialowicz
     * @see #sortListByDate(ObservableList, ListView, Path, Path, int)
     * @see #sortListByName(ObservableList, ListView, int)
     */


    public void rightSortBy(){
        if(sortByDateRightSideDown.isSelected()){
            sortListByDate(fileList2, listView2, rightAreaPath, leftAreaPath, 1);
        }
        if(sortByNameRightSideDown.isSelected()){
            sortListByName(fileList2, listView2, 1);
        }
        if(sortByDateRightSideUp.isSelected()){
            sortListByDate(fileList2, listView2, rightAreaPath, leftAreaPath, -1);
        }
        if(sortByNameRightSideUp.isSelected()){
            sortListByName(fileList2, listView2, -1);
        }
    }


    /***
     * This method loads files from directory to observableArrayList, which later is given as parameter
     * to ListView.
     *
     * @author Michal Musialowicz
     *
     * @param listOfFiles - list of all files from directory.
     * @param files - observableArray of files, to which listOfFiles' elements will be added
     * @param pathToDir - path to current directory
     */


    public void loadFiles(File[] listOfFiles, List<String> files, Path pathToDir){
        files.clear();
        files.add("[...]");
        if(listOfFiles == null){
            return;
        }
        for(File file: listOfFiles){
            Path pathExpanded = Paths.get(pathToDir.toString() + "\\" + file.getName());
            BasicFileAttributes attr = null;
            try {
                attr = Files.readAttributes(pathExpanded, BasicFileAttributes.class);
            } catch (IOException e){
                System.out.println("ERROR, line 443");
            }

            FileTime time = attr.creationTime();
            String modifiedTime = time.toString();
            String[] tempArray = modifiedTime.split("T");
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append(tempArray[0]);
            stringBuilder.append("   ");
            String[] anotherTempArray = tempArray[1].split("\\.");
            stringBuilder.append(anotherTempArray[0]);

            if(file.isDirectory()){
                files.add(file.getName() + " ;;;  <DIR>   " + "(created on: " + stringBuilder.toString() + ")");
            } else {
                files.add(file.getName() + " ;;;  " + "    (created on: " + stringBuilder.toString() + ")");
            }
        }
    }


    /***
     * calls createNewDirectory() method when user presses F7
     * and calls deleteFileOrDirectory() when user presses F8
     *
     * @author Michal Musialowicz
     * @param keyEvent - KeyEvent listener, that listens for pressed key.
     * @see #deleteFileOrDirectory()
     * @see #createNewDirectory()
     */


    @FXML
    public void keyHandler(KeyEvent keyEvent){
        if(keyEvent.getCode().equals(KeyCode.F7)) {
            createNewDirectory();
        }
        if(keyEvent.getCode().equals(KeyCode.F8)){
            deleteFileOrDirectory();
        }
    }


    /***
     * This method is called when user presses F7 on keyboard. It creates a new window in which user has to provide
     * name of the new directory and choose if is meant to be created in left area directory or in right area
     * directory
     *
     *
     * @author Michal Musialowicz
     * @see CreateDirectory#getFileName()
     * @see CreateDirectory#getPath()
     */


    @FXML
    public void createNewDirectory(){
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(mainWindow.getScene().getWindow());
        FXMLLoader createDirectoryLoader = new FXMLLoader();
        createDirectoryLoader.setLocation(getClass().getResource("fxml/createDirectory.fxml"));
        dialog.setTitle("Create New Directory");
        dialog.setHeaderText("Creating New Directory:");
        try {
            dialog.getDialogPane().setContent(createDirectoryLoader.load());
        } catch (IOException exception){
            System.out.println("Couldn't load dialog..");
        }
        dialog.getDialogPane().getButtonTypes().add(ButtonType.APPLY);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CANCEL);
        ((Button) dialog.getDialogPane().lookupButton(ButtonType.APPLY)).setText("Create Directory");
        CreateDirectory controller = createDirectoryLoader.getController();
        Optional<ButtonType> result = dialog.showAndWait();
        String fileName = null;
        String whichPath = null;
        boolean exit = true;
        if(result.isPresent() && result.get() == ButtonType.APPLY){
            fileName = controller.getFileName();
            whichPath = controller.getPath();
            exit = false;
        }
        if(exit){
            return;
        }


        StringBuilder sb = new StringBuilder();
        if(whichPath.equals("LEFT")){
            sb.append(leftAreaPath.toString());
        } else {
            sb.append(rightAreaPath.toString());
        }
        sb.append("\\" + fileName);
        Path path = Paths.get(sb.toString());
        if(new File(path.toString()).exists() || fileName.equals("[...]")){
            Alert errorAlert =  new Alert(Alert.AlertType.ERROR);
            errorAlert.setTitle("ERROR.");
            errorAlert.setHeaderText("Error while creating new directory!");
            errorAlert.setContentText("File Manager couldn't create new directory. " +
                    "It either already exists or you don't have permission to do so.");
            errorAlert.show();
            return;
        }
        try {
            Files.createDirectory(path);
        } catch (IOException e) {
            System.out.println("ERROR, line 510.");
            return;
        }
        BasicFileAttributes attr;
        try {
            attr = Files.readAttributes(path, BasicFileAttributes.class);
        } catch (IOException e){
            return;
        }

        FileTime time = attr.creationTime();
        String modifiedTime = time.toString();
        String[] tempArray = modifiedTime.split("T");
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(tempArray[0]);
        stringBuilder.append("   ");
        String[] anotherTempArray = tempArray[1].split("\\.");
        stringBuilder.append(anotherTempArray[0]);
        if(whichPath.equals("LEFT")) {
            fileList1.add(fileName + " ;;;  <DIR>   " + "(created on: " + stringBuilder.toString() + ")");
            if (leftAreaPath.equals(rightAreaPath)) {
                fileList2.add(fileName + " ;;;  <DIR>   " + "(created on: " + stringBuilder.toString() + ")");
            }
        } else {
            fileList2.add(fileName + " ;;;  <DIR>   " + "(created on: " + stringBuilder.toString() + ")");
            if (leftAreaPath.equals(rightAreaPath)) {
                fileList1.add(fileName + " ;;;  <DIR>   " + "(created on: " + stringBuilder.toString() + ")");
            }
        }
        leftSortBy();
        rightSortBy();
	}


    /***
     * Deletes file/directory (depends on the selected item from list) when user presses F8 on keyboard.
     * The method first figures out which file from which list is going to be deleted, checks if it
     * isn't path to precedent directory, creates a confirmation alert to check if user is sure to
     * delete chosen content. If the user confirms, the method removes file from list(s),
     * calls deleteDirectoryRecursion(Path path) method and sorts both lists by using leftSortBy()
     * and rightSortBy() methods.
     *
     * @author Michal Musialowicz
     * @see #leftSortBy()
     * @see #rightSortBy()
     * @see #deleteDirectoryRecursion(Path)
     */


    @FXML
    public void deleteFileOrDirectory(){

        int which = 1;
        String item = listView1.getSelectionModel().getSelectedItem();
        if(item == null){
            item = listView2.getSelectionModel().getSelectedItem();
            which = 2;
            if(item == null){
                return;
            }
        }
        if(item.equals("[...]")){
            return;
        }

        Alert confirmationAlert = new Alert(Alert.AlertType.CONFIRMATION);
        String[] tempitems = item.split(" ;;;");
        confirmationAlert.setTitle("Deleting File/Directory.");
        confirmationAlert.setHeaderText("Warning!");
        confirmationAlert.setContentText("Are you sure you wish to delete " + tempitems[0] + "? The operation is irreversible.");
        ((Button) confirmationAlert.getDialogPane().lookupButton(ButtonType.OK)).setText("Delete.");
        ((Button) confirmationAlert.getDialogPane().lookupButton(ButtonType.CANCEL)).setText("Go back.");
        Optional<ButtonType> result = confirmationAlert.showAndWait();
        if(result.isPresent() && result.get() == ButtonType.CANCEL){
            return;
        }
        StringBuilder sb = new StringBuilder();
        if(which == 1){
            sb.append(leftAreaPath.toString());
        } else {
            sb.append(rightAreaPath.toString());
        }
        String[] parts = item.split(" ;;;");
        sb.append("\\" + parts[0]);
        if(!new File(sb.toString()).exists()){
            return;
        }

        Path path = Paths.get(sb.toString());
        try {
            if(which == 1) {
                if (parts[1].contains("<DIR>")) {
                    fileList1.remove(item);
                    if (leftAreaPath.toString().equals(rightAreaPath.toString())) {
                        fileList2.remove(item);
                    }
                    listView1.setItems(fileList1);
                    listView2.setItems(fileList2);
                } else {
                    fileList1.remove(item);
                    if (leftAreaPath.toString().equals(rightAreaPath.toString())) {
                        fileList2.remove(item);
                    }
                    listView1.setItems(fileList1);
                    listView2.setItems(fileList2);
                }
            } else {
                if (parts[1].contains("<DIR>")) {
                    fileList2.remove(item);
                    if (rightAreaPath.toString().equals(leftAreaPath.toString())) {
                        fileList1.remove(item);
                    }
                    listView1.setItems(fileList1);
                    listView2.setItems(fileList2);
                } else {
                    fileList2.remove(item);
                    if (rightAreaPath.toString().equals(leftAreaPath.toString())) {
                        fileList1.remove(item);
                    }
                    listView1.setItems(fileList1);
                    listView2.setItems(fileList2);
                }
            }
            deleteDirectoryRecursion(path);
            leftSortBy();
            rightSortBy();
        } catch (IOException e){
            System.out.println("Error while deleting.");
        }
    }


    /***
     *
     * Deletes all contents of directory and directory itself. I am not author of this method,
     * SEE! <a href="https://softwarecave.org/2018/03/24/delete-directory-with-contents-in-java/">Source of the code: </a>
     *
     * @author Robert Piasecki from softwarecave.com.
     * @param path - path to file/directory that is going to be deleted.
     * @throws IOException
     */

    public void deleteDirectoryRecursion(Path path) throws IOException {
        if (Files.isDirectory(path, LinkOption.NOFOLLOW_LINKS)) {
            try (DirectoryStream<Path> entries = Files.newDirectoryStream(path)) {
                for (Path entry : entries) {
                    deleteDirectoryRecursion(entry);
                }
            }
        }
        Files.delete(path);
    }


    /***
     *
     * copies directory from one directory to another directory.
     * @author Michal Musialowicz
     *
     * @param dirName - name of the directory to be copied.
     * @param whichPath - if whichPath parameter is set to 1, directory from left area directory is copied to
     *                        right area directory. If parameter is set to 2, directory from right area directory
     *                        is copied to left area directory.
     */


    public void copyDir(String dirName, int whichPath){
        String[] parts = dirName.split(" ;;;");
        dirName = parts[0];
        File file1 = new File(leftAreaPath.toString() + "\\" + dirName);
        File file2 = new File(rightAreaPath.toString() + "\\" + dirName);
        if(whichPath == 1){
            if(!file2.exists()){
                file2.mkdir();
            } else {
                return;
            }
        } else {
            if (!file1.exists()){
                file1.mkdir();
            } else {
                return;
            }
        }
        try{
            if(whichPath == 1){
                FileUtils.copyDirectory(file1, file2);
            } else {
                FileUtils.copyDirectory(file2, file1);
            }
        } catch (IOException e){
            System.out.println("BLAD");
        }
    }


    /***
     *
     * copies file from one directory to another.
     * @author Michal Musialowicz
     *
     * @param fileName - name of the file to be copied.
     * @param whichPath - if whichPath parameter is set to 1, file from left area directory is copied to
     *                  right area directory. If parameter is set to 2, file from right area directory is copied
     *                  to left area directory.
     */


    public void copyFile(String fileName, int whichPath){
        String[] parts = fileName.split(" ;;;");
        fileName = parts[0];
        File file1 = new File(leftAreaPath.toString() + "\\" + fileName);
        File file2 = new File(rightAreaPath.toString() + "\\" + fileName);
        try{
            if(whichPath == 1){
                FileUtils.copyFile(file1, file2);
            } else {
                FileUtils.copyFile(file2, file1);
            }
        } catch (IOException e){
            return;
        }
    }




    /***
     * sets all labels' texts when program is started.
     * @author Michal Musialowicz
     */


    public void setLabels(){
        sortByDateLeftSideDown.setText("Sort By Date \u2193");
        sortByDateLeftSideUp.setText("Sort By Date \u2191");
        sortByDateRightSideUp.setText("Sort By Date \u2191");
        sortByDateRightSideDown.setText("Sort By Date \u2193");
        sortByNameLeftSideDown.setText("Sort By Name \u2193");
        sortByNameLeftSideUp.setText("Sort By Name \u2191");
        sortByNameRightSideUp.setText("Sort By Name \u2191");
        sortByNameRightSideDown.setText("Sort By Name \u2193");
        leftPathLabel.setText("Path: " + leftAreaPath.toString());
        rightPathLabel.setText("Path: " + rightAreaPath.toString());
    }

}



