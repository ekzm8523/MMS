package com.company.Controller;

import com.company.Model.Message;
import com.company.View.LoginViewPanel;
import com.company.View.ViewManager;
import com.google.gson.Gson;


import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

//import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MainController extends Thread {

    private Logger logger; // 모르면 사용한해도 될듯..

    // Message 객체를 json 객체로 파싱하기 위한 Gson 객체 생성
    private Gson gson = new Gson();

    private Socket s;

    // 입출력 스트림
    private BufferedReader inMsg = null; // 서버가 보낸 메시지를 읽는 버퍼
    private PrintWriter outMsg = null; // 서버에 메시지를 보낼 버퍼

    private boolean status;

    private Message m;

    private Thread thread;

    public MainController() {
        // 로거 객체 초기화
        logger = Logger.getLogger(this.getClass().getName());

    }

    public void connectServer() { // 서버에 접속하기
        try {
            s = new Socket("127.0.0.1", 7777);
            logger.log(Level.INFO, "[Client]Server 연결 성공!!");
            inMsg = new BufferedReader(new InputStreamReader(s.getInputStream()));
            outMsg = new PrintWriter(s.getOutputStream(), true);

            thread = new Thread(this);
            thread.start();

        } catch (Exception e) {
            logger.log(Level.WARNING, "[MultiChatUI]connectServer() Exception 발생!!");
            e.printStackTrace();
        }
    }

    // 각 컨트롤로에서 maincontroller의 객체를 사용해 msgSend
    public void msgSend(Message msg) {
            outMsg.println(gson.toJson(msg));
    }

    public void run() {
        String msg;
        status = true; // 종료 시 false 제어 되게끔하기...

        while (status) {
            try {
                msg = inMsg.readLine();

                m = gson.fromJson(msg, Message.class);
                switch (m.getType()) {
                    case 2 :
                        LoginViewPanel lvp = ViewManager.getInstance().getMainView().loginViewPanel;
                        String id = lvp.txtId.getText();
                        String pw = lvp.txtPw.getText();
                        if(m.getId().equals(id) && m.getPasswd().equals(pw)) {
                            ProgramManager.getInstance().id = m.getId();
                            ProgramManager.getInstance().pw = m.getPasswd();
                            ProgramManager.getInstance().setMainState();
                        }
                        break;
                    case 4 :
                        ViewManager.getInstance().getChattingView().refreshData(m.getMsg());
                        break;
                    case 5 : // 상품 등록
                        try {
                            ProgramManager.getInstance().getPC().refreshData();}
                        catch(Exception e1){}
                        break;
                    case 6 : // 상품 수정
                        try {
                            ProgramManager.getInstance().getPC().refreshData();}
                        catch(Exception e1){}
                        break;
                    case 7 : // 상품 삭제
                        try {
                            ProgramManager.getInstance().getPC().refreshData();}
                        catch(Exception e1){}
                        break;
                    case 8 : // 고객 등록
                        ViewManager.getInstance().getMainView().customerViewPanel.drawTextArea(m.getMsg());
                        break;
                    case 9 : break; // 고객 수정
                    case 10 : break; // 고객 삭제

                    case 18 : // 주문 성공
                    {
                        System.out.println("클라이언트 체크 : " + "/" + m.getId());
                        if(m.getId().equals(ProgramManager.getInstance().id)) { // 만약 해당 클라이언트가 서버에서 보낸 메세지의 id에 해당하는 경우
                            ProgramManager.getInstance().getOrderController().OrderItems(ViewManager.getInstance().getShoppingView(), ProgramManager.getInstance().getShoppingController().getTotal()); // 주문, 주문 내역 쿼리 실행
                            ProgramManager.getInstance().getCC().savePoint(ViewManager.getInstance().getShoppingView().txtPhone.getText(), (int)(ProgramManager.getInstance().getShoppingController().getTotal()*0.01)); // 고객 포인트 업데이트

                            try {
                                ProgramManager.getInstance().getPC().refreshData();} // 상품 화면 업데이트
                            catch(Exception e1){}

                            try {
                                ProgramManager.getInstance().getShoppingController().payComplete(ViewManager.getInstance().getShoppingView()); // 클라이언트의 쇼핑창에 메세지 전달
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            } catch (ClassNotFoundException e2) {
                                e2.printStackTrace();
                            }

                            ViewManager.getInstance().getShoppingView().repaint();
                        }
                        break;
                    }
                    case 19 : // 주문 실패
                    {
                        if(m.getId().equals(ProgramManager.getInstance().id)) { // 만약 해당 클라이언트가 서버에서 보낸 메세지의 id에 해당하는 경우
                            try {
                                ProgramManager.getInstance().getShoppingController().payFailed(ViewManager.getInstance().getShoppingView()); // 클라이언트의 쇼핑창에 메세지 전달
                            } catch (SQLException e1) {
                                e1.printStackTrace();
                            } catch (ClassNotFoundException e2) {
                                e2.printStackTrace();
                            }

                        }
                        break;
                    }

                    case 15 : // 에러 메시지
                        LoginViewPanel loginPanel = ViewManager.getInstance().getMainView().loginViewPanel;
                        loginPanel.txtId.setText("");
                        loginPanel.txtPw.setText("");

                }
            } catch (IOException e) {
                logger.log(Level.WARNING, "[MultiChatUI]메세지 스트림 종료!!");
            }
        }
        logger.info("[MultiChatUI]" + thread.getName() + " 메세지 수신 스레드 종료됨!!");
    }

    public static void main(String[] args) { // 서버에 접속하기
        ProgramManager manager = ProgramManager.getInstance();
        manager.getMainController().connectServer();
        manager.setLoginState();

    }
}