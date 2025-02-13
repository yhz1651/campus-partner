<script setup>
import { reactive, ref } from "vue";
import request from "@/utils/request";
import {useUserStore} from "@/stores/user";
import '@wangeditor/editor/dist/css/style.css'

const pageNum = ref(1)
const pageSize = ref(15)
const total = ref(0)

const userStore = useUserStore()
const token = userStore.getBearerToken
const auths =  userStore.getAuths

const state = reactive({
  tableData: [],
})

const load = () => {
  request.get('/information/page', {
    params: {
      pageNum: pageNum.value,
      pageSize: pageSize.value
    }
  }).then(res => {
    state.tableData = res.data.records
    total.value = res.data.total
  })
}
load()  // 调用 load方法拿到后台数据


</script>

<template>
  <div style="width: 60%; margin: auto">

    <el-card style="margin: 10px 0">
      <div style="display: flex; padding-bottom: 10px; border-bottom: 1px solid #ccc; margin-bottom: 10px">
        <div style="font-size: 20px; font-weight: bold; color: orange">
          校园公告
        </div>
      </div>
      <el-table :data="state.tableData" stripe :show-header="false">
        <el-table-column prop="name" label="标题">
          <template #default="scope">
            <a target="_blank" :href="'/front/Infodetail?id=' + scope.row.id" v-if="scope.row.id">{{ scope.row.name }}</a>
          </template>
        </el-table-column>
        <el-table-column prop="time" label="发布时间"></el-table-column>

      </el-table>
    </el-card>

    <!--    动态分页    -->
    <el-card style="margin: 10px 0; background-color: white">
      <div style="display: flex">
        <div>
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
      </div>
    </el-card>

  </div>
</template>