
import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by dilli on 4/17/2017.
 */
public class Record extends JFrame{
    private JTextField ConsignorNameTextField;
    private JTextField PriceTextField;
    private JTextField TitleTextField;
    private JTextField ArtistNameTextField;
    private JTextField PhoneNoTextField;
    private JComboBox AddToComboBox;
    private JComboBox DisplayComboBox;
    private JPanel rootPanel;
    private JButton addRecordButton;
    private JButton showRecordButton;
    private JComboBox ReturnComboBox;
    private JButton deleteRecordButton;
    private JList<RecordObject> InventoryJList;
    private JList<RecordObjectSold> SoldItemJList;
    private JList<BargainListObject> BargainJList;
    private JList<PayInfoObject> PayInfoJList;
    private JLabel DisplayDaysJLabel;
    private JButton displayNoOfDaysButton;
    private JLabel NotifyConsignorLabel;
    private DefaultListModel<RecordObject> InventoryListModel;
    private DefaultListModel<RecordObjectSold> SoldItemListModel;
    private DefaultListModel<PayInfoObject> PayInfoListModel;
    private DefaultListModel<BargainListObject> BargainListModel;
    private RecordDBcontroller recordDBcontroller;

    Record(RecordDBcontroller recordDBcontroller){
        setPreferredSize(new Dimension(800,400));
        this.recordDBcontroller = recordDBcontroller;
        InventoryListModel = new DefaultListModel<RecordObject>();
        SoldItemListModel = new DefaultListModel<RecordObjectSold>();
        PayInfoListModel = new DefaultListModel<PayInfoObject>();
        BargainListModel = new DefaultListModel<BargainListObject>();
        InventoryJList.setModel(InventoryListModel);
        SoldItemJList.setModel(SoldItemListModel);
        PayInfoJList.setModel(PayInfoListModel);
        BargainJList.setModel(BargainListModel);
        setContentPane(rootPanel);
        addItemsToComboBox();
        pack();
        setVisible(true);
        addListeners();
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

    }
    private void addItemsToComboBox(){
        AddToComboBox.addItem("Inventory");
        AddToComboBox.addItem("Sold Item");
        AddToComboBox.addItem("Bargain List");
        AddToComboBox.addItem("Donate Item");

        DisplayComboBox.addItem("All Inventory Records");
        DisplayComboBox.addItem("Sold Items");
        DisplayComboBox.addItem("Pay Information");
        DisplayComboBox.addItem("Bargain List Item");
        DisplayComboBox.addItem("No. of days in Inventory List");

        ReturnComboBox.addItem("Yes");
        ReturnComboBox.addItem("No");
    }
    private void addListeners(){
        addRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                //Read data, send message to database via controller
                String cName = ConsignorNameTextField.getText();
                String phone = PhoneNoTextField.getText();
                String aName = ArtistNameTextField.getText();
                String title = TitleTextField.getText();
                java.util.Date recordDate = new java.util.Date();
                java.sql.Date sqlrecordDate = new java.sql.Date(recordDate.getTime());
                if(AddToComboBox.getSelectedItem().equals("Inventory")) {
                    if (cName.isEmpty()) {
                        JOptionPane.showMessageDialog(Record.this, "Enter Consignor's name");
                        return;
                    }else if(phone.isEmpty()){
                        JOptionPane.showMessageDialog(Record.this,"Enter the phone number");
                        return;
                    }else if(aName.isEmpty()){
                        JOptionPane.showMessageDialog(Record.this,"Enter name of the artist");
                        return;
                    }else if(title.isEmpty()){
                        JOptionPane.showMessageDialog(Record.this,"Enter the title");
                        return;
                    }
                    double price;

                    try {
                        price = Double.parseDouble(PriceTextField.getText());
                    } catch (NumberFormatException nfe) {
                        JOptionPane.showMessageDialog(Record.this, "Enter the number for price");
                        return;
                    }
                    RecordObject recordObjectRecord = new RecordObject(cName, phone, aName, title, price,sqlrecordDate);
                    recordDBcontroller.addRecordToDatabase(recordObjectRecord);
                    //Clear input JTextFields
                    ConsignorNameTextField.setText("");
                    PhoneNoTextField.setText("");
                    ArtistNameTextField.setText("");
                    TitleTextField.setText("");
                    PriceTextField.setText("");
                    JOptionPane.showMessageDialog(Record.this,"A record has been add to inventory list");

                }
//                else {
//                    JOptionPane.showMessageDialog(Record.this,"Please select Inventory from Add To combo box");
//                    return;
//                }
                if (AddToComboBox.getSelectedItem().equals("Sold Item") && InventoryJList.getSelectedValue()!=null){
                    String sPriceString = JOptionPane.showInputDialog(Record.this,"Please enter the selling price");
                    double sPrice = Double.parseDouble(sPriceString);
                    double consignorShare = sPrice*0.4;
                    RecordObject recordSold = InventoryJList.getSelectedValue();
                    String conName = recordSold.getConsignorName();
                    String sTitle = recordSold.getTitle();
                    Date soldDate = new Date();
                    java.sql.Date soldDateSQL = new java.sql.Date(soldDate.getTime());
                    RecordObjectSold recordObjectSold = new RecordObjectSold(conName,sTitle,sPrice,soldDateSQL);
                    recordDBcontroller.addRecordToSoldTable(recordObjectSold);
                    recordDBcontroller.delete(recordSold);//deletes selected object from Inventory list after it is added to sold item list.
                    JOptionPane.showMessageDialog(Record.this,"The selected item has been moved to sold item list");
                    String payNowString = JOptionPane.showInputDialog(Record.this,"Please enter the amount you want to pay now to the consignor");
                    double payNow  = Double.parseDouble(payNowString);
                    double amountOwed = consignorShare - payNow;
                    PayInfoObject payInfoObject = new PayInfoObject(conName,consignorShare,payNow,amountOwed);
                    recordDBcontroller.addRecordToPayInfoTable(payInfoObject);
                    //RecordObject ro = InventoryJList.getSelectedValue();
                    //SoldItemListModel.addElement(recordObjectSold);

                }
                if(AddToComboBox.getSelectedItem().equals("Bargain List")){
                    java.sql.Date recordAddedDate = InventoryJList.getSelectedValue().getDate();
                    Date now = new Date();
                    long date = now.getTime() - recordAddedDate.getTime();
                    long msInDay = 1000*60*60*24;
                    long days = date/msInDay;
                    String StringDays = Long.toString(days);
                    int intDays = Integer.parseInt(StringDays);
                    int maxDaysInInventory = 30;

                    if(intDays<=maxDaysInInventory) {
                        JOptionPane.showMessageDialog(Record.this, "Oops! can't add this item to Bargain list before 30 days\n" +
                                "of it's stay in Inventory");
                    }else{
                    String conName = InventoryJList.getSelectedValue().getConsignorName();
                    String artistName = InventoryJList.getSelectedValue().getArtistName();
                    String rTitle = InventoryJList.getSelectedValue().getTitle();
                    double basePrice = 1;
                    BargainListObject bargainListObject = new BargainListObject(conName,rTitle, artistName,basePrice);
                    recordDBcontroller.addRecordToBargainTable(bargainListObject);
                     }
                }
//                else{
//                    JOptionPane.showMessageDialog(Record.this,"Oops! missing correct selection");
//                }

            }
        });
        showRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if(DisplayComboBox.getSelectedItem().equals("All Inventory Records")){
                    //request all data from database to update list
                    ArrayList<RecordObject> allData = recordDBcontroller.getAllData();
                    setListData(allData);
                }
                if(DisplayComboBox.getSelectedItem().equals("Sold Items")){
                    ArrayList<RecordObjectSold> allSoldData = recordDBcontroller.getAllSoldData();
                    setListOfSoldData(allSoldData);
                }
                if(DisplayComboBox.getSelectedItem().equals("Pay Information")){
                    ArrayList<PayInfoObject> allPayInfoData = recordDBcontroller.getAllPayInfoData();
                    setListOfPayInfoData(allPayInfoData);
                }
                if(DisplayComboBox.getSelectedItem().equals("No. of days in Inventory List")){
                    if(InventoryJList.getSelectedValue()!=null) {
                        displayNumberOfDays();
                    }else {
                        JOptionPane.showMessageDialog(Record.this,"Please select an item from the Inventory list to display number of days");
                    }
                }
                if(DisplayComboBox.getSelectedItem().equals("Bargain List Item")){
                    ArrayList<BargainListObject> allBargainListData = recordDBcontroller.getAllBargainListData();
                    setListOfBargainListData(allBargainListData);
                }
            }
        });
        deleteRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                RecordObject recordObject = InventoryJList.getSelectedValue();
                if (recordObject == null) {
                    JOptionPane.showMessageDialog(Record.this, "Please select the record to delete");
                } else if (!ReturnComboBox.getSelectedItem().equals("Yes")) {
                    JOptionPane.showMessageDialog(Record.this, "Please select 'Yes' from the 'Return to Consignor?' combox box");
                }
                if (recordObject!=null && ReturnComboBox.getSelectedItem().equals("Yes")){
                    int delete = JOptionPane.showConfirmDialog(Record.this, "Are you sure you want to delete this item?",
                            "Delete", JOptionPane.OK_CANCEL_OPTION);
                    if(delete == JOptionPane.OK_OPTION) {
                        recordDBcontroller.delete(recordObject);
                        ArrayList<RecordObject> recordObject1 = recordDBcontroller.getAllData();
                        setListData(recordObject1);
                        JOptionPane.showMessageDialog(Record.this, "The selected item has been deleted from the list");
                    }
                }
            }
        });
        displayNoOfDaysButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                displayNumberOfDays();
            }
        });
        showRecordButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            }
        });
    }
    private void displayNumberOfDays(){
        java.sql.Date displayDays = InventoryJList.getSelectedValue().getDate();
        Date now = new Date();
        long date = now.getTime() - displayDays.getTime();
        long msInDay = 1000*60*60*24;
        long days = date/msInDay;
        String StringDays = Long.toString(days);
        int intDays = Integer.parseInt(StringDays);
        DisplayDaysJLabel.setText(StringDays);
        if(intDays>30){
            NotifyConsignorLabel.setText("Notify consignor to pick item");
        }else{
            int daysDiff = 30-intDays;
            String daysDiffString = Integer.toString(daysDiff);
            NotifyConsignorLabel.setText(daysDiffString+" days to stay in this list");
        }
    }
    void setListData(ArrayList<RecordObject> data) {
        InventoryListModel.clear();
        //add cubes object to display list model.
        for (RecordObject rob : data) {
            InventoryListModel.addElement(rob);
        }
    }
    void setListOfSoldData(ArrayList<RecordObjectSold> soldData){
        SoldItemListModel.clear();
        for(RecordObjectSold robs : soldData){
            SoldItemListModel.addElement(robs);
        }
    }
    void setListOfPayInfoData(ArrayList<PayInfoObject> payInfoData){
        PayInfoListModel.clear();
        for(PayInfoObject pob : payInfoData){
            PayInfoListModel.addElement(pob);
        }
    }
    void setListOfBargainListData(ArrayList<BargainListObject> bargainListData){
        BargainListModel.clear();
        for(BargainListObject bob: bargainListData){
            BargainListModel.addElement(bob);
        }
    }
}