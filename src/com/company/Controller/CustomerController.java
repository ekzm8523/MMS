package com.company.Controller;

import com.company.Model.CustomerDAO;
import com.company.Model.CustomerDTO;
import com.company.Model.Message;
import com.company.View.CustomerManageView;
import com.company.View.CustomerViewPanel;
import com.company.View.MainView;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;

public class CustomerController extends Thread{
    public CustomerDAO cdao = new CustomerDAO();
    String bufferedString = null;
    CustomerDTO customer = null;
    CustomerManageView cmv;
    CustomerViewPanel cvp;
    boolean search = false, register = false, update = false, delete = false, isClick = false;
    private static CustomerController s_Instance;

    public void appMain() {
        cvp = ProgramManager.getInstance().getMainView().customerViewPanel;
        cmv = ProgramManager.getInstance().getCustomerManageView();

        if(search) {
            cvp.initDTModel();
            String phoneNum = cvp.txtPhoneNum.getText();
            if(phoneNum.equals("")) {
                searchAllCustomer();
            } else searchCustomer(phoneNum);
            search = false;
        }
        if(register) {
            registerCustomer();
            register = false;
        }
        if(update) {
            updateCustomer(bufferedString);
            update = false;
        }
        if(delete) {
            deleteCustomer();
            delete = false;
        }
        if(isClick) {
            int row = cvp.tblCustomerList.getSelectedRow();
            bufferedString = (String)cvp.dtmodel.getValueAt(row, 0);
            isClick = false;
        }
    }

    public CustomerController() {
    }

    public class DeleteButtonListener implements  ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            deleteCustomer();
        }
    }

    public class SearchButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            cvp.initDTModel();
            String phoneNum = cvp.txtPhoneNum.getText();
            if(phoneNum.equals("")) {
                searchAllCustomer();
            } else searchCustomer(phoneNum);
        }
    }

    public class RegisterButtonListener implements  ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            registerCustomer();
            cmv.refreshTextField();
            cvp.taNewCustomer.append(customer.getCName() + "님이 새로 등록되었습니다." + "\n");
        }
    }

    public class ExitButtonListener implements  ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            cmv.dispose();
        }
    }

    public class AddButtonListener implements ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            makeCustomerManageView();
        }
    }

    public class UpdateButtonListener implements  ActionListener {

        @Override
        public void actionPerformed(ActionEvent e) {
            updateCustomer(bufferedString);
        }
    }

    public class TableClickListener implements MouseListener {

        public void mouseClicked(MouseEvent e) {
            int row = cvp.tblCustomerList.getSelectedRow();
            bufferedString = (String)cvp.dtmodel.getValueAt(row, 0);
        }
        public void mousePressed(MouseEvent e) { }
        public void mouseReleased(MouseEvent e) { }
        public void mouseEntered(MouseEvent e) { }
        public void mouseExited(MouseEvent e) { }
    }

    public CustomerManageView makeCustomerManageView() {
        cmv = new CustomerManageView();
        cmv.drawView();

        return cmv;
    }

    public void searchAllCustomer() {
        ArrayList<CustomerDTO> datas = cdao.getAll();
        if(datas != null) {
            for(CustomerDTO c : datas) {
                String line[] = {c.getPhoneNum(), c.getCName(), String.valueOf(c.getCPoint())};
                cvp.addRowToTable(line);
            }
        }
    }

    public void searchCustomer(String phoneNum) {
        String cName, cPoint;
        customer = cdao.getCustomer(phoneNum);
        if(customer != null) {
            cName = customer.getCName();
            cPoint = String.valueOf(customer.getCPoint());
            String row[] = {phoneNum, cName, cPoint};
            cvp.addRowToTable(row);
        } else {
            JOptionPane.showMessageDialog(cvp, "등록되지 않은 회원 입니다.");
        }
    }

    public void updateCustomer(String bufferedString ) {
        int row = cvp.tblCustomerList.getSelectedRow();
        if(row == -1 || bufferedString == null) {
            JOptionPane.showMessageDialog(cvp, "수정할 정보를 선택해 주세요.");
        } else {
            String phoneNum = (String)cvp.dtmodel.getValueAt(row, 0);
            String cName = (String)cvp.dtmodel.getValueAt(row, 1);
            int cPoint = Integer.parseInt((String)cvp.dtmodel.getValueAt(row, 2));
            String msg = cName  + "/" + "update Customer set phone_num = " + "'" + phoneNum + "'" + ", c_name = " + "'" + cName + "'" + ", c_point = " + cPoint + " where phone_num = " + bufferedString;
            ProgramManager.getInstance().getMainController().msgSend(new Message("","",msg,9));
        }
    }

    public void deleteCustomer() {
        int row = cvp.tblCustomerList.getSelectedRow();
        if(row == -1) {
            JOptionPane.showMessageDialog(cvp, "삭제할 정보를 조회 후 선택해 주세요.");
        } else {
            String phoneNum = (String)cvp.dtmodel.getValueAt(row, 0);
            String cName = (String)cvp.dtmodel.getValueAt(row, 1);
            String msg = cName + "/" + "delete from Customer where phone_num = " + "'" + phoneNum + "'";
            ProgramManager.getInstance().getMainController().msgSend(new Message("","",msg,10));
        }
    }

    public void registerCustomer() {
        String cName = cmv.txtName.getText();
        String phoneNum = cmv.txtPhone.getText();
        String cPoint = cmv.txtPoint.getText();
        if (cName.equals("") || phoneNum.equals("") || cPoint.equals("")) {
            JOptionPane.showMessageDialog(cmv, "빈 칸을 채워 주세요.");
        }else {
            int point = Integer.parseInt(cPoint);
            String msg = cName + "/" + "insert into Customer(phone_num, c_name, c_point) values(" + phoneNum + ", " + "'" + cName + "'" + ", " + cPoint + ")";
            System.out.println(msg);
            ProgramManager.getInstance().getMainController().msgSend(new Message("","",msg, 8));
            cmv.refreshTextField();
        }
    }

    public void savePoint(String phone, int point) {
        String msg = "아무개" + "/" + "update Customer set c_point = " + point + " where phone_num = " + phone;
        ProgramManager.getInstance().getMainController().msgSend(new Message("","",msg, 9));
    }

}
