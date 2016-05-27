/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
function HouseInfoMonitor() {
    this.Init();
}
HouseInfoMonitor.prototype = {
    m_Socket: null,
    m_IsConnect: false,
    m_IsLogin: false,
    OnOpen: function (event)
    {
        console.log("OnOpen");
        this.m_IsConnect = true;
    },
    OnReceive: function (event)
    {
        //console.log("OnReceive");
        var recvMsg = this.ParseData(event.data);
        switch (recvMsg.Action)
        {
            case ServerAction.SVMO_LOGIN:
                {
                    if (recvMsg.Args[0] === "0")
                    {
                        console.log("Login Fail");
                        alert("Login Fail");
                        return;
                    }
                    console.log("Login Success");
                    alert("Login Success");
                    this.m_IsLogin = true;
                }
                break;
        }
    },
    OnError: function (event)
    {
        console.log("OnError");
    },
    OnClose: function (event)
    {
        console.log("OnClose");
        alert("Server Disconnect!!");
        this.m_IsLogin = false;
    },
    Send: function (msg)
    {
        this.m_Socket.Send(msg.GetString());
    },
    ParseData: function (recvString)
    {
        var args = recvString.split(/[\|;]/);
        var msg = new Message();
        msg.Action = parseInt(args[0]);
        for (var i = 1; i < args.length; i++)
        {
            msg.Args.push(args[i]);
        }
        return msg;
    },
    Connect: function ()
    {
        this.m_Socket.Connect(ServerIP + ":" + ServerPort);
    },
    Login: function ()
    {
        if (!this.m_IsConnect)
        {
            console.log("No connection");
            return;
        }
        var newMsg = new Message();
        newMsg.Action = ServerAction.MOSV_LOGIN;
        newMsg.Args.push("Monitor");
        this.Send(newMsg);
    },
    Init: function ()
    {
        this.m_Socket = new WrapWebSocket();
        this.m_Socket.m_Event.AddListener("onOpen", BindWrapper(this, this.OnOpen));
        this.m_Socket.m_Event.AddListener("onReceive", BindWrapper(this, this.OnReceive));
        this.m_Socket.m_Event.AddListener("onError", BindWrapper(this, this.OnError));
        this.m_Socket.m_Event.AddListener("onClose", BindWrapper(this, this.OnClose));
        this.Connect();
        $("#DIV_Version").append('<p class="text-right">Version : <strong>' + Version + '</strong></p>');
    }
};


