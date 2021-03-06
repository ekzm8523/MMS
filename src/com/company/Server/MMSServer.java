package com.company.Server;

import com.company.Model.AccountDAO;
import com.company.Model.CustomerDAO;
import com.google.gson.Gson;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.sql.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.logging.Logger;


public class MMSServer {
    Connection conn;
    PreparedStatement pstmt;
    ResultSet rs;
    String userid = "jaewon";
    String pwd = "wlfkf132";
    String sql;
    String jdbcDriver = "com.mysql.cj.jdbc.Driver";
    String jdbcUrl = "jdbc:mysql://mms.crgsa3qt3jqa.ap-northeast-2.rds.amazonaws.com/mms?user=jaewon&password=wlfkf132";


    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------
    static final int INSERT_ACCOUNT = 1, LOGIN = 2, LOGOUT = 3, CHATTING = 4, NEWCUSTOMER = 8, UPDATECUSTOMER = 9, DELETECUSTOMER = 10, ERROR = 15;
    // 1: 계정 추가, 2: 로그인, 3: 로그아웃, 4: 채팅 메시지, 8: 고객 추가, 9: 고객 수정, 10 : 고객 삭제, 15: 에러 메시지
    static final int ADD_PRODUCT =5; // 상품 등록
    static final int UPDATE_PRODUCT =6; // 상품 수정
    static final int DELETE_PRODUCT = 7; // 상품 삭제
    static final int ORDER = 11; // 주문 테이블 갱신
    static final int ORDERHISTORY = 16; // 주문 내역 테이블 갱신
    static final int PAYTRY = 17; // 구매 시도
    static final int ORDERCOMPLETE = 18; // 주문 성공 시
    static final int ORDERFAIL = 19; // 재고 부족으로 주문 실패 시..'
    // -------------------------------------------------------------------------------
    // -------------------------------------------------------------------------------

    private ServerSocket ss = null;
    private Socket s = null; // client를 받기 위한 매개체
    private CustomerDAO cdao = new CustomerDAO();

    int orderCode; // 주문 내역을 위한 변수

    // 연결된 client 스레드를 관리하는 ArrayList
    ArrayList<MMSThread> mmsThreadList = new ArrayList<MMSThread>();

    Logger logger; // 로거 객체 선언

    public void connectDB() { // DB연결
        try {
            Class.forName(jdbcDriver);
            System.out.println("드라이버 로드 성공");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            System.out.println("데이터베이스 연결 준비...");
            conn = DriverManager.getConnection(jdbcUrl, userid, pwd);
            System.out.println("데이터베이스 연결 성공");
        } catch (SQLException e1) {
            e1.printStackTrace();
        }
    }

    public void closeDB() { // DB 연결 종료
        try {
            pstmt.close();
            if(rs!=null) rs.close();
            conn.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void start() { // 서버 시작
        logger = Logger.getLogger(this.getClass().getName());

        try {
            Collections.synchronizedList(mmsThreadList); // 리스트 동기화 하여 관리
            ss = new ServerSocket(7777); // 임의의 포트번호를 통해 서버 소켓 생성
            logger.info("MultiChatServer start");

            while (true) {
                s = ss.accept(); // 클라이언트가 접속할 때까지 기다림

                MMSThread chat = new MMSThread();
                mmsThreadList.add(chat);
                chat.start(); // 클라이언트와 연결 된 쓰레드 동작
            }

        } catch (Exception e) {
            logger.info("[MultiChatServer]start() Exception 발생!!");
            e.printStackTrace();
        }
    }

    public static void main(String args[]) { // 서버 실행
        MMSServer multiChatServer = new MMSServer();
        multiChatServer.start();
    }

    class MMSThread extends Thread {
        private boolean status = true;

        String msg;
        Message m = new Message();

        Gson gson = new Gson(); // Message 객체를 json 객체로 파싱하기 위한 Gson 객체 생성

        // 입출력 스트림
        private BufferedReader inMsg = new BufferedReader(new InputStreamReader(s.getInputStream()));
        private PrintWriter outMsg = new PrintWriter(s.getOutputStream(), true);

        public String id = null;

        public MMSThread() throws IOException {
        }

        public void run() {
            while (status) { // 상태 정보가 true 일경우 반복문을 돌며 수신된 메세지 처리
                try {
                    msg = inMsg.readLine(); // 메세지 수신
                    m = gson.fromJson(msg, Message.class); // Message 클래스로 매핑
                    String str[] = {};
                    switch(m.getType()) {
                        case CHATTING: // 메시지 내용 브로드 캐스트
                            str = m.getMsg().split("/");
                            msgSendAll(gson.toJson(new Message("","",str[0] + " : " + str[1],CHATTING)));
                            break;

                        case NEWCUSTOMER: // 고객 추가
                            str = m.getMsg().split("/");
                            connectDB();
                            pstmt = conn.prepareStatement(str[1]);
                            if(pstmt.executeUpdate() != 0) {
                                msgSendAll(gson.toJson(new Message("", "", str[0] + "님이 등록되었습니다.", NEWCUSTOMER)));
                                // 추가된 고객을 모든 클라이언트에게 알려 고객 알림창에 띄워주기
                            }
                            closeDB();
                            break;
                        case UPDATECUSTOMER: // 고객 수정
                            connectDB();
                            pstmt = conn.prepareStatement(m.getMsg());
                            System.out.println(m.getMsg());
                            if(pstmt.executeUpdate() != 0) {
                                msgSendAll(gson.toJson(new Message("", "", "정보가 수정되었습니다.", UPDATECUSTOMER)));
                            }
                            closeDB();
                            break;
                        case DELETECUSTOMER: // 고객 삭제
                            connectDB();
                            pstmt = conn.prepareStatement(m.getMsg());
                            if(pstmt.executeUpdate() != 0) {
                                msgSendAll(gson.toJson(new Message("", "", "정보가 삭제되었습니다.", DELETECUSTOMER)));
                            }
                            closeDB();
                            break;
                        case INSERT_ACCOUNT: // 계정 생성
                            connectDB();
                            pstmt = conn.prepareStatement(m.getMsg());
                            if(pstmt.executeUpdate() != 0) {
                                msgSendAll(gson.toJson(new Message("","","계정 생성 완료",INSERT_ACCOUNT)));
                            }
                            closeDB();
                            break;
                        case LOGIN: // 로그인
                            connectDB();

                            pstmt = conn.prepareStatement(m.getMsg());
                            rs = pstmt.executeQuery();

                            if(rs.next()) { // 일치하는 계정이 있으면 로그인 상태를 true로 만들고, 다른 클라이언트의 채팅창에 메시지 보내기
                                AccountDAO adao = new AccountDAO();
                                adao.setLogin(m.getId(),m.getPasswd());
                                msgSendAll(gson.toJson(new Message(m.getId(), m.getPasswd(), m.getId() + "님이 로그인 하셨습니다.", LOGIN)));
                                msgSendAll(gson.toJson(new Message("","",m.getId() + "님이 접속하셨습니다.", CHATTING)));
                                logger.info("id : " + m.getId() + " pw : " + m.getPasswd());
                            }
                            closeDB();
                            break;
                        case LOGOUT: // 로그인상태를 false로 만들고, 다른 클라이언트의 채팅창에 메시지 보내기
                            connectDB();
                            AccountDAO adao = new AccountDAO();
                            adao.setLogout(m.getId(), m.getPasswd());
                            msgSendAll(gson.toJson(new Message(m.getId(), m.getPasswd(), m.getId() + "님이 로그아웃 하셨습니다.", LOGOUT)));
                            msgSendAll(gson.toJson(new Message("","",m.getId() + "님이 접속종료 하셨습니다.", CHATTING)));
                            logger.info("id :" + id + " logout");
                            closeDB();
                            break;
                        case ADD_PRODUCT: // 상품 등록
                            connectDB();
                            pstmt = conn.prepareStatement(m.getMsg());
                            if (pstmt.executeUpdate() != 0) {
                                msgSendAll(gson.toJson(new Message("", "", "상품이 등록되었습니다.", ADD_PRODUCT)));
                                closeDB();
                            }

                            break; //Add

                        case UPDATE_PRODUCT: // 상품 수정
                            connectDB();
                            pstmt = conn.prepareStatement(m.getMsg());
                            if (pstmt.executeUpdate() != 0) {
                                msgSendAll(gson.toJson(new Message("", "", "상품이 수정되었습니다.", UPDATE_PRODUCT)));
                                closeDB();
                            }
                            break;
                        case DELETE_PRODUCT: // 상품 삭제
                            connectDB();
                            System.out.println("삭제에 성공했나?");
                            pstmt =conn.prepareStatement(m.getMsg());
                            if( pstmt.executeUpdate() != 0 ){

                                msgSendAll(gson.toJson(new Message("", "", "상품이 삭제되었습니다.", DELETE_PRODUCT)));
                                closeDB();
                            }

                            break;

                        case ORDER : // 클라이언틀에서 주문 요청 시 주문 내역을 업데이트 + 해당 주문의 ordercode 저장
                            connectDB();
                            try {
                                pstmt = conn.prepareStatement(m.getMsg());
                                pstmt.executeUpdate();

                                // 가장 최신의 입력된 auto_increment 값 가져오기
                                // --> mysql에서는 해당 값을 컨넥션 별로 관리하므로 멀티 스레드 구현 시 race condition같은 문제를 걱정할 필요없음
                                // 즉, 락을 걸거나 트랜잭션을 구현할 필요가 X
                                sql = "select last_insert_id()";
                                pstmt = conn.prepareStatement(sql);
                                rs = pstmt.executeQuery();

                                if(rs.next()) orderCode = rs.getInt(1);
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            closeDB();
                            break;

                        case PAYTRY : // 클라이언트가 결제하기 버튼을 눌렀을 경우 물품 재고가 구매를 원하는 개수보다 많은지 여부에 따른 반환 값 메세지로 전송
                            connectDB();
                            str = m.getMsg().split("@");
                            int cnt = Integer.parseInt(str[1]); // 구매 예정인 즉, 업데이트 할 품목 개수
                            System.out.println("품목개수 : " + cnt);
                            boolean canBuy = true;
                            String str1[] = str[0].split("/"); // 3쌍(업데이트 될 수량, 구매 수량, 상품 코드)이 세트이며 cnt 즉, 물품 개수만큼 반복해 재고 검사
                            for(int i=1; i<=cnt; i++) {
                                try {
                                    sql = "select * from Product where pr_code = ?";
                                    pstmt = conn.prepareStatement(sql);
                                    pstmt.setInt(1, Integer.parseInt(str1[i*3-1]));
                                    System.out.println("코드번호 : " + Integer.parseInt(str1[i*3-1]));
                                    rs = pstmt.executeQuery();

                                    if(rs.next()) { // 순회하며 재고와 구매 수량 비교 후 canBuy의 boolean 값 결정
                                        if(rs.getInt("amount") < Integer.parseInt(str1[i*3-2])) {
                                            System.out.println("품목별 재고 : " + rs.getInt("amount"));
                                            canBuy = false;
                                        }
                                    }
                                } catch (SQLException e){
                                    e.printStackTrace();
                                }
                            }

                            System.out.println(canBuy);

                            if(canBuy) { // 재고가 충분해 구매가 가능한 경우 상품개수 업데이트 하고 Order내역과 OrderHistory내역, Customer 테이블의 정보를 갱신할 수 있도록 클라이언트에 메세지 전달
                                for(int i=1; i<=cnt; i++) {
                                    try {
                                        sql = "update Product set amount = ? where pr_code = ?";
                                        pstmt = conn.prepareStatement(sql);
                                        pstmt.setInt(1, Integer.parseInt(str1[i*3-3]));
                                        pstmt.setInt(2, Integer.parseInt(str1[i*3-1]));
                                        System.out.println(Integer.parseInt(str1[i*3-3]) + "/" + Integer.parseInt(str1[i*3-2]) + "/" + Integer.parseInt(str1[i*3-1]));
                                        pstmt.executeUpdate();
                                    } catch (SQLException e){
                                        e.printStackTrace();
                                    }
                                }
                                msgSendAll(gson.toJson(new Message(m.getId(), "", "", ORDERCOMPLETE))); // 구매 고객 아이디로 보내기 --> 주문 성공 메세지
                            }
                            else msgSendAll(gson.toJson(new Message(m.getId(), "", "", ORDERFAIL))); // 구매 고객 아이디로 보내기 --> 제고 부족으로 인한 주문 실패 메세지

                            System.out.println("서버에서 아이디는 : " + m.getId());
                            closeDB();
                            break;

                        case ORDERHISTORY : // 클라이언트에서 주문 상세 내역 쿼리문 실행 요청 시 msg에 해당하는 쿼리문 실행
                            connectDB();
                            str = m.getMsg().split("/");
                            try {
                                sql = str[0] + orderCode + str[1];
                                pstmt = conn.prepareStatement(sql);
                                pstmt.executeUpdate();
                            } catch (SQLException e) {
                                e.printStackTrace();
                            }
                            closeDB();
                            break;
                    }
                } catch (IOException | SQLException e) {
                    e.printStackTrace();
                    closeDB();
                    status = false;
                }
            }
            this.interrupt(); // 동작이 끝나면 종료시켜주기
            logger.info(this.getName() + " 종료됨!!");
        }

        public void msgSendAll(String msg) { // 메시지를 브로드캐스트 하기
            for (MMSThread ct : mmsThreadList) {
                ct.outMsg.println(msg);
            }
        }
    }
}


