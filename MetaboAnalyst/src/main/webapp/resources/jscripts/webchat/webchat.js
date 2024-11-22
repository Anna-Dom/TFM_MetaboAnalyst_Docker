/* 
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
var sessionId = "";
var calledCurl = false;
var botAnsweredBool = false;
var check = false;
var search_type = '';
var webchatObj = (function () {
    return {
        func1: function (embeddedBool, prefix) {
            if (document.getElementsByClassName("rw-widget-container")[0] != null) {
                return;
            }
            if(prefix === "disabled"){
                return;
            }
            ;
            !(function () {
                let e = document.createElement("script"),
                        t = document.head || document.getElementsByTagName("head")[0];

                var pathArray = window.location.href.split('/');
                var protocol = pathArray[0];
                var host = pathArray[2];
                var url = protocol + '//' + host;
                var page = getCurrentPageUrl();
                var category = "metaboanalyst";
                var payLoad = "/prof_website";
                if (page != "home" && page != "") {
                    payLoad = payLoad + '{"page":"' + page + '","recommandation_filter":"'+ category +'"}';
                } else {
                    payLoad = payLoad + '{"page":"home","recommandation_filter":"'+ category +'"}';
                }
                
                console.log(prefix + "=====================prefix");
                (e.src =
                        url + "/MetaboAnalyst/resources/jscripts/webchat/rasa-webchat.js"),
                        (e.async = !0),
                        (e.onload = () => {
                            var chatbot = window.WebChat.default(
                                    {
                                        initPayload: payLoad, //EXTERNAL_work_flow_suggestions
                                        customData: {language: "en"},
                                        socketUrl: "https://"+ prefix +".omicsbot.ca",
                                        
//                                        tooltipPayload: "{\"name\": \"\prof_website\",  \"entities\":{\"page\": \"home\"}}", tooltipDelay: 100,
//                                        disableTooltips: true,
                                        title: ' 💬',
                                        subtitle: '',
                                        showMessageDate: true, showFullScreenButton: true, displayUnreadCount: true,
                                        inputTextFieldHint: 'Enter key words here...',
                                        hideWhenNotConnected: false,
                                        embedded: false,
                                        connectOn: 'open',
                                        onSocketEvent: {"bot_uttered": () => botAnswered()},
                                        onWidgetEvent: {"onChatOpen": () => callCurlCommand()},
                                        params: {"storage": "session"}
                                        // add other props here↩
                                    },
                                    null
                                    );
                        })

                t.insertBefore(e, t.firstChild);
            })();
        }
    }

})(webchatObj || {});
// var parameters = "{\"name\": \"prof_website\",  \"entities\":{\"page\": \"entity_value\"}}";
// var url_sessionid = "https://www.omicsbot.ca/conversations/19dcb8ae575447748cbdba384a22af51/trigger_intent?output_channel=latest"
//function callCurlCommand(parameters) {
//    
//        var url_sessionid = "https://www.omicsbot.ca/conversations/" + sessionId + "/trigger_intent?output_channel=latest"; 
//    console.log("Session ID in callCurl");
//    console.log(sessionId);
//   $.ajax({
//        beforeSend: function () {
//            $.messager.progress({
//                text: 'Processing .....'
//            });
//        },
//        dataType: "html",
//        type: "POST",
//        url: '/MetaboAnalyst/faces/AjaxCall',
//        data: {function: 'callCurlCommand', parameters: parameters, url_sessionid: url_sessionid},
//        async: false,
//        success: function (result) {
//            console.log(result);
//            console.log("Success");
//        },
//        error: function(){console.log("error");}
//    });
//}

function getCurrentPageUrl() {
    const currentUrl = window.location.href;
    var n = currentUrl.lastIndexOf('/');
    var result = currentUrl.substring(n + 1);
    result = result.replace(".xhtml", "");
    return result;
}

function changePos() {
    var load = document.getElementsByClassName("lds-ring")[0];
//                load.style.display = "block";

    const container = document.getElementById("rasadiv");
    setTimeout(function () {
        if (container !== null || container !== undefined) {
            var rasa = document.getElementById("rasaWebchatPro");
            if (rasa !== null) {
                //rasa.style.height = "400px";
                //container.insertBefore(rasa, container.firstChild);

                //load.style.display = "none";

                return;
            }

        }
    }, 2000);
}

function emitMessage(e, t) {
//    var iFrame = document.getElementById('iframe_net');
//console.log(t);
//    if (typeof iFrame !== "undefined") {
//        iFrame.contentWindow.processBotMessage(t.message);
//    }
}

function sendMsg(msg) {
    console.log(msg);
    const webchatRef = window.WebChat;
    const textArea = document.getElementsByClassName("rw-new-message")[0];
    textArea.value = msg;
    const ke = new KeyboardEvent('keydown', {
        bubbles: true, cancelable: true, keyCode: 13
    });
    textArea.dispatchEvent(ke);
}
function setSessionId(id) {
    sessionId = id;
    //callCurlCommand();
}

function botAnswered() {
    botAnsweredBool = true;
}

function callCurlCommand() {
    setTimeout(function () {

        if (botAnsweredBool) {
            return;
        }

        var page = getCurrentPageUrl();
        var parameters = ""
        if (page === "home" || page === "") {
            parameters = "{\"name\": \"prof_website\"}"
        }

        var parameters = "{\"name\": \"prof_website\", \"entities\":{\"page\": \"" + page + "\"}}";

        var url_sessionid = "https://dev.omicsbot.ca/conversations/" + sessionId + "/trigger_intent?output_channel=latest";
        var res = fetch(url_sessionid, {
            method: 'POST',
            headers: {
                //'C-H Content-Type': 'application/json',
                'Content-Type': 'application/json'
            },
            body: parameters
        });
        calledCurl = true;
    }, 4000)


}