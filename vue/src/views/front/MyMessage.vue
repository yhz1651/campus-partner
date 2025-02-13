<script setup>
import {defineEmits, reactive, ref} from "vue";
import {ElMessage} from "element-plus";
import request from "@/utils/request";
import {useUserStore} from "@/stores/user";
import router from "@/router";
const pageNum = ref(1)
const pageSize = ref(10)
const total = ref(0)

const userStore = useUserStore()
const token = userStore.getBearerToken
const auths =  userStore.getAuths
let user = userStore.getUser
const activePath = router.currentRoute.value.path

const state = reactive({
  tableData: []
})

// let $myEmit = defineEmits(['getUnread'])
const load = () => {
  request.get('/message/page', {
    params: {
      type: 'user',
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  }).then(res => {
    state.tableData = res.data.records
    total.value = res.data.total
    // $myEmit('getUnread')  // 发送从新获取未读消息 并更新数字的指令
  })
}
load()  // 调用 load方法拿到后台数据

request.put('/message/read')  // 发起请求


</script>

<template>
  <div style="width: 60%; margin: auto; display: flex">
    <!--  目录  -->
    <div style="width: 200px; border-radius: 20px">
      <el-card>
        <el-menu
            :default-active="activePath"
            mode="vertical"
            router
            style="border: none; height: 100%"
        >
          <el-menu-item index="/front/myDynamic">我的动态</el-menu-item>
          <el-menu-item index="/front/myReserve">我的活动预约</el-menu-item>
          <el-menu-item index="/front/myCollect">我的收藏</el-menu-item>
          <el-menu-item index="/front/myFollow">我的关注</el-menu-item>
          <el-menu-item index="/front/myFans">我的粉丝</el-menu-item>
          <el-menu-item index="/front/myMessage">我的消息</el-menu-item>
        </el-menu>
      </el-card>
    </div>

    <!--  内容  -->
    <div style="flex: 1; margin-left: 20px">
      <el-card style="min-height: calc(100vh - 100px)">
        <div style="display: flex; padding-bottom: 10px; border-bottom: 1px solid #ccc;">
          <div style="font-size: 20px; font-weight: bold; color: orange">
            我的消息
          </div>
        </div>
        <div style="">
          <el-table :data="state.tableData" stripe :show-header="false">

            <el-table-column label="内容" width="400px">
              <template #default="scope">
                <div v-html="scope.row.content"></div>
              </template>
            </el-table-column>
            <el-table-column prop="time" label="通知时间"></el-table-column>
          </el-table>
        </div>

        <div style="margin: 20px 0">
          <el-pagination
              @current-change="load"
              @size-change="load"
              v-model:current-page="pageNum"
              v-model:page-size="pageSize"
              background
              :page-sizes="[5, 10, 20]"
              layout="total, prev, pager, next"
              :total="total"
          />
        </div>
      </el-card>
    </div>

  </div>


</template>