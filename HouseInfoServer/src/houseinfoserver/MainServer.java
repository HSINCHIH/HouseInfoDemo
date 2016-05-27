/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package houseinfoserver;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map.Entry;

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
    HashMap<String, String> m_DBFile = new HashMap();

    public MainServer() {
        m_HostIP = Utilities.GetHostIP();
        System.out.println(String.format("Host IP : %s", m_HostIP));
        ReadDBFile();
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
            case ServerAction.MOSV_LOGIN: {
                MOSV_LOGIN_recv(msg, ep);
            }
            break;
            case ServerAction.CLSV_NOFITY: {
                CLSV_NOFITY_recv(msg, ep);
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

    private void ReadDBFile() {
        try {
            BufferedReader br = new BufferedReader(new InputStreamReader(new FileInputStream("DBFile.txt"), "UTF8"));
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null) {
                sb.append(line);
                sb.append(System.lineSeparator());
                line = br.readLine();
            }
            String[] dataRows = sb.toString().split("\r|\n");
            for (String row : dataRows) {
                if (row.isEmpty()) {
                    continue;
                }
                String key = row.substring(0, row.indexOf(","));
                if (m_DBFile.containsKey(key)) {
                    continue;
                }
                m_DBFile.put(key, row);
            }
            int lengrh = dataRows.length;
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "ReadDBFile", e.getMessage()));
        }
    }

    private void DISCONNECTED_recv(BaseMessage msg, EndPoint ep) {
        m_Log.Writeln(String.format("%s : %s", "DISCONNECTED_recv", ep.toString()));
        if (m_CLientList.containsKey(ep.toString())) {
            String sessionID = m_CLientList.get(ep.toString());
            m_CLientList.remove(ep.toString());
            m_Log.Writeln(String.format("%s : %s %s", "Client Remove", ep.toString(), sessionID));
        }
        if (m_MonitorList.containsKey(ep.toString())) {
            String sessionID = m_MonitorList.get(ep.toString());
            m_MonitorList.remove(ep.toString());
            m_Log.Writeln(String.format("%s : %s %s", "Monitor Remove", ep.toString(), sessionID));
        }
    }

    private void CLSV_LOGIN_recv(BaseMessage msg, EndPoint ep) {
        try {
            m_CLientList.put(ep.toString(), msg.Args.get(0));
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVCL_LOGIN;
            newMsg.Args.add("1");
            m_WebSocketServerEP.Send(newMsg, ep);
            m_Log.Writeln(String.format("%s : %s %s", "Client Login", ep.toString(), msg.Args.get(0)));
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "CLSV_LOGIN_recv", e.getMessage()));
        }
    }

    private void MOSV_LOGIN_recv(BaseMessage msg, EndPoint ep) {
        try {
            m_MonitorList.put(ep.toString(), msg.Args.get(0));
            BaseMessage newMsg = new BaseMessage();
            newMsg.Action = ServerAction.SVMO_LOGIN;
            newMsg.Args.add("1");
            m_WebSocketServerEP.Send(newMsg, ep);
            m_Log.Writeln(String.format("%s : %s %s", "Monitor Login", ep.toString(), msg.Args.get(0)));
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "MOSV_LOGIN_recv", e.getMessage()));
        }
    }

    private void CLSV_NOFITY_recv(BaseMessage msg, EndPoint ep) {
        try {
            String dbKey = msg.Args.get(1);
            BaseMessage newMsgClient = null;
            if (!m_DBFile.containsKey(dbKey)) {
                //send result for client
                newMsgClient = new BaseMessage();
                newMsgClient.Action = ServerAction.SVCL_NOFITY;
                newMsgClient.Args.add("0");
                newMsgClient.Args.add("Can't find key in DBFile..");
                m_WebSocketServerEP.Send(newMsgClient, ep);
                return;
            }
            String dbData = m_DBFile.get(dbKey);
            //send to monitor
            BaseMessage newMsgMonitor = new BaseMessage();
            newMsgMonitor.Action = ServerAction.SVMO_NOFITY;
            newMsgMonitor.Args.add(msg.Args.get(0));
            newMsgMonitor.Args.add(dbData);
            for (Entry<String, String> entry : m_MonitorList.entrySet()) {
                String key = entry.getKey();
                EndPoint sendEP = new EndPoint(key);
                m_WebSocketServerEP.Send(newMsgMonitor, sendEP);
            }
            //send result for client
            newMsgClient = new BaseMessage();
            newMsgClient.Action = ServerAction.SVCL_NOFITY;
            newMsgClient.Args.add("1");
            m_WebSocketServerEP.Send(newMsgClient, ep);
        } catch (Exception e) {
            m_Log.Writeln(String.format("%s Exception : %s", "MOSV_LOGIN_recv", e.getMessage()));
        }
    }
}
