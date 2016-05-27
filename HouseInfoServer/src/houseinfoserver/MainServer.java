/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package houseinfoserver;

/**
 *
 * @author mark.chen
 */
public class MainServer implements IReceiveMsgCallBack {

    Log m_Log = new Log("MainServer");
    String m_HostIP = "";
    WebSocketServerEP m_WebSocketServerEP = null;

    public MainServer() {
        m_HostIP = Utilities.GetHostIP();
        System.out.println(String.format("Host IP : %s", m_HostIP));
    }

    @Override
    public void ReceiveMsg(BaseMessage msg, EndPoint ep) {
        switch (msg.Action) {
            case ServerAction.DISCONNECTED: {
                DISCONNECTED_recv(msg, ep);
            }
            break;
            default: {
                m_Log.Writeln(String.format("%s UnKnown message : %s", "ReceiveMsg", msg.toString()));
            }
            break;
        }
    }

    public boolean Start() {
        try {
            m_Log.Writeln("Start");
            //Control client server
            m_WebSocketServerEP = new WebSocketServerEP();
            m_WebSocketServerEP.SetMsgCallBack(this);
            if (!m_WebSocketServerEP.Start(IPAddress.Any, Settings.ServerPort)) {
                m_Log.Writeln(String.format("%s %s", "Start", "m_WebSocketServerEP fail"));
                return false;
            }
            return true;
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "Start", e.getMessage()));
            return false;
        }
    }

    public void Stop() {
        m_WebSocketServerEP.Stop();
    }

    private void DISCONNECTED_recv(BaseMessage msg, EndPoint ep) {
    }

    private void PLSV_LOGIN_recv(BaseMessage msg, EndPoint ep) {
//        try {
//
//        } catch (Exception e) {
//            m_Log.Writeln(String.format("%s Exception : %s", "PLSV_LOGIN_recv", e.getMessage()));
//        }
    }
}
