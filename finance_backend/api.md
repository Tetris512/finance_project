全是POST接口

1. **聊天接口** (/api/chat)

**request:**

{

  "prompt" : "奖学金申请流程",

  ”user":"tetris512"

}

**response(流式返回):**

返回数据部分截图：

{"event":"done"}

{"chunk":"。"}

{"chunk":"ju.edu.cn/"}

{"chunk":"://ndcwc.n"}

{"chunk":"大学校园网环境下访问 https"}

{"chunk":"详细信息，请在南京"}



其中：{"event":"done"}是结束信号





2. **反馈接口** (/api/feedback)

**request:**

{

​	 "user" : "tetris512",

​	"rating" : "down",

​	"conversation_id" : "bdcaffcc-3ff1-4f7c-b328-1136c32c57c8", **(不知道前端能否做到？)**

​	"feedbackText" : "回答不是很准确"

}





3. 获取当前用户的历史聊天信息接口(/api/history)

**request:**

{	

​	"user" : "tetris512",

​	"limit": 20

}

其中limit默认值为20，可缺少



**response:**

{

  "data": [

​    {

​      "conversation_id": "bdcaffcc-3ff1-4f7c-b328-1136c32c57c8",

​		

​      "query": "draw a cat",

​	  "answer" : "I have generated an image of a cat for you. Please check your messages to view the image."

​    },

​	{......}

  ]

}

返回一个列表list，每个列表表示一个会话对象，包含它的id，用户的提问内容query，AI的回答answer



