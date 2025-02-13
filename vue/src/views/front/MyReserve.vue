<script setup>
import { reactive, ref } from "vue";
import request from "@/utils/request";
import {ElMessage} from "element-plus";
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
  tableData: [],
})

const load = () => {
  request.get('/reserve/page', {
    params: {
      type: 'user',
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  }).then(res => {
    state.tableData = res.data.records
    total.value = res.data.total
  })
}
load()  // 调用 load方法拿到后台数据

const reset = () => {
  name.value = ''
  load()
}

// 取消预约
const handleCancelReserve = (id) => {
  request.delete('/reserve/' + id).then(res => {
    if (res.code === '200') {
      ElMessage.success('操作成功')
      load()  // 刷新表格数据
    } else {
      ElMessage.error(res.msg)
    }
  })
}

</script>

<template>
  <div style="margin: auto; width: 60%; display: flex">
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
    <div style="flex: 1; margin-left: 20px;">
      <el-card style="min-height: calc(100vh - 100px)">
        <div style="display: flex; padding-bottom: 10px; border-bottom: 1px solid #ccc;">
          <div style="font-size: 20px; font-weight: bold; color: orange">
            我的活动预约
          </div>
        </div>
        <!--  表格   -->
        <div style="margin: 10px 0">
          <el-table :data="state.tableData" stripe border>
            <el-table-column type="index" label="序号" width="100px"></el-table-column>
            <el-table-column label="活动名称">
              <template #default="scope">
                {{ scope.row.activity.name }}
              </template>
            </el-table-column>
            <!--        <el-table-column prop="userId" label="用户id"></el-table-column>-->
            <el-table-column label="预约人">
              <template #default="scope">
                {{ scope.row.user.name }}
              </template>
            </el-table-column>
            <el-table-column prop="time" label="预约时间"></el-table-column>
            <el-table-column prop="status" label="审核状态">
              <template #default="scope">
                <el-tag v-if="scope.row.status === '待审核'">待审核</el-tag>
                <el-tag v-if="scope.row.status === '审核通过'" type="success">审核通过</el-tag>
                <el-tag v-if="scope.row.status === '审核不通过'" type="danger">审核不通过</el-tag>
              </template>
            </el-table-column>

            <el-table-column label="取消" width="150" v-if="auths.includes('reserve.cancel')">
              <template #default="scope">
                <el-button type="primary" @click="handleCancelReserve(scope.row.id)" v-if="auths.includes('reserve.cancel')" :disabled="scope.row.status !== '待审核'">取消预约</el-button>
              </template>
            </el-table-column>

          </el-table>
        </div>

        <!--  分页   -->
        <div style="margin: 10px 0" v-if="total">
          <el-pagination
              @current-change="load"
              @size-change="load"
              v-model:current-page="pageNum"
              v-model:page-size="pageSize"
              background
              layout="total, prev, pager, next"
              :total="total"
          />
        </div>
      </el-card>

    </div>
  </div>

</template>