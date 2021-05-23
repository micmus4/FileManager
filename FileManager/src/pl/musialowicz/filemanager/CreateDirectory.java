package pl.musialowicz.filemanager;

import javafx.fxml.FXML;
import javafx.scene.control.RadioButton;
import javafx.scene.control.TextField;

public class CreateDirectory {

    @FXML
    private RadioButton leftPathRB, rightPathRB;

    @FXML
    private TextField fileNameField;


    /***
     *
     * retrieves text from TextField when creating new directory.
     *
     * @author Michal Musialowicz
     * @return text from the TextField
     */

    public String getFileName(){
        return fileNameField.getText();
    }


    /***
     * retrieves 'LEFT' String when leftPathRB is selected, or 'RIGHT' when rightPathRB is selected.
     *
     * @return path as text
     */
    public String getPath(){
        if(leftPathRB.isSelected()){
            return "LEFT";
        } else {
            return "RIGHT";
        }
    }
}
