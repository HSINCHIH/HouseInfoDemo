/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package houseinfoserver;

import java.util.HashMap;

/**
 *
 * @author mark.chen
 */
public class MainServer implements IReceiveMsgCallBack {

    Log m_Log = new Log("MainServer");
    String m_HostIP = "";
    WebSocketServerEP m_WebSocketServerEP = null;
    HashMap<String, String> m_CLientList = new HashMap();
    HashMap<String, String> m_MonitorList = new HashMap();

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
            case ServerAction.CLSV_LOGIN: {
                CLSV_LOGIN_recv(msg, ep);
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
        m_Log.Writeln(String.format("%s : %s", "DISCONNECTED_recv", ep.toString()));
        if (m_CLientList.containsKey(ep.toString())) {
            String clientID = m_CLientList.get(ep.toString());
            m_CLientList.remove(ep.toString());
            m_Log.Writeln(String.format("%s : %s %s", "Remove", ep.toString(), clientID));
        }
    }

    private void CLSV_LOGIN_recv(BaseMessage msg, EndPoint ep) {
        try {
            m_CLientList.put(ep.toString(), msg.Args.get(0));
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVCL_LOGIN;
            newMsg.Args.add("1");
            m_WebSocketServerEP.Send(newMsg, ep);
            m_Log.Writeln(String.format("%s : %s %s", "Login", ep.toString(), msg.Args.get(0)));
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "CLSV_LOGIN_recv", e.getMessage()));
        }
    }
}
