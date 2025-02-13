<script setup>
import {nextTick, onMounted, ref} from "vue";
import V3Emoji from 'vue3-emoji'
import 'vue3-emoji/dist/style.css'
import {useUserStore} from "@/stores/user";
import request from "@/utils/request";
import router from "@/router";
import {ElMessage} from "element-plus";

const messages = ref([])

const userStore = useUserStore()
const user = userStore.getUser

const text = ref('')  // 聊天输入的内容
const divRef = ref()   // 聊天框的引用

// 页面滚动到最新位置的函数
const scrollBottom = () => {
  nextTick(() => {   // 等到页面元素出来之后再去滚动
    divRef.value.scrollTop = divRef.value.scrollHeight
  })
}

// 页面加载完成触发此函数
onMounted(() => {
  request.get("/im/init/20").then(res => {
    messages.value = res.data

    scrollBottom()
  })
})

const client = new WebSocket(`ws://localhost:9090/imserver/${user.uid}`)
// 发送消息触发滚动条滚动
const send = () => {
  if (client) {
    client.send(text.value)
  }
  text.value = ''  // 清空文本框
  ElMessage.success('发送成功，积分+1')
}

const optionsName = {
  'Smileys & Emotion': '笑脸&表情',
  'Food & Drink': '食物&饮料',
  'Animals & Nature': '动物&自然',
  'Travel & Places': '旅行&地点',
  'People & Body': '人物&身体',
  Objects: '物品',
  Symbols: '符号',
  Flags: '旗帜',
  Activities: '活动'
}

client.onopen = () => {
  console.log('open')
}
client.onclose = () => {  // 页面刷新的时候和后台websocket服务关闭的时候
  console.log('close')
}
client.onmessage = (msg) => {
  if (msg.data) {
    let json = JSON.parse(msg.data)
    if (json.uid && json.text) {  // 聊天消息
      messages.value.push(json)
      scrollBottom()  // 滚动页面到最底部
    }
  }
}
</script>

<template>
  <div style="width: 60%; margin: auto">
    <el-card>
      <div style="font-size: 20px; font-weight: bold; color: orange; margin-bottom: 10px">
        公共聊天室
      </div>
      <div ref="divRef" style="background-color: white; padding: 20px; border: 1px solid #ccc; border-radius: 10px; height: 550px; overflow-y: scroll;">
        <div v-for="item in messages" :key="item.id">
          <!--    左侧 别人   -->
          <div style="display: flex; margin: 20px 0;" v-if="user.uid !== item.uid">
            <el-popover
                placement="top-start"
                :width="200"
                trigger="hover"
            >
              <template #reference>
                <img :src="item.user.avatar" alt="" @click="router.push('/front/userDetail?id=' + item.user.id)"
                     style="width: 40px; height: 40px; border-radius: 50%; margin-right: 10px; cursor: pointer">
              </template>
              <div style="line-height: 20px">
                <div style="font-size: 16px; color: black; font-weight: bold">{{ item.user.name }}</div>
                <div style="font-size: 12px;">{{ item.user.profile }}</div>
              </div>
            </el-popover>
            <div style="display: flex; flex-direction: column">
              <div style="font-size: 10px; color: #888888; margin-left: 5px">{{ item.user.name }}</div>
              <div style="line-height: 30px; background-color: lightgrey; padding: 0 10px; width:fit-content; border-radius: 10px; ">
                {{ item.text }}
              </div>
            </div>
          </div>

          <!--    右侧 自己   -->
          <div style="display: flex; justify-content: flex-end; margin: 20px 0;" v-else>
            <div style="display: flex; flex-direction: column">
              <div style="font-size: 10px; color: #888888; margin-right: 5px; text-align: right">{{ item.user.name }}</div>
              <div style="line-height: 30px; background-color: lightgreen; padding: 0 10px; width:fit-content; border-radius: 10px; color: black">
                {{ item.text }}
              </div>
            </div>
            <el-popover
                placement="top-start"
                :width="200"
                trigger="hover"
            >
              <template #reference>
                <img :src="item.user.avatar" alt="" style="width: 40px; height: 40px; border-radius: 50%; margin-left: 10px; cursor: pointer;"
                     @click="router.push('/front/userDetail?id=' + item.user.id)">
              </template>
              <div style="line-height: 20px">
                <div style="font-size: 16px; color: black; font-weight: bold">{{ item.user.name }}</div>
                <div style="font-size: 12px;">{{ item.user.profile }}</div>
              </div>
            </el-popover>
          </div>
        </div>

      </div>
      <div style="margin: 20px 0; width: 100%">
        <V3Emoji default-select="recent" :recent="true" :options-name="optionsName" :keep="true" :textArea="true"
                 size="mid" v-model="text"/>
        <div style="text-align: right">
          <el-button @click="send" type="primary">发送</el-button>
        </div>
      </div>
    </el-card>


  </div>
</template>
