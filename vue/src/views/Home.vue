<script setup>
import {reactive} from "vue";
import request from "@/utils/request";

const state = reactive({
  notice: []
})

const load = () => {
  request.get("/notice").then(res => {
    state.notice = res.data
  })
}
load()
</script>

<template>
  <div>
    <div>
      <el-card style="width: 50%;  margin: 10px auto">
        <template #header>
          <div>
            <span>公告列表</span>
          </div>
        </template>
        <el-collapse accordion>
          <el-collapse-item v-for="(item,index) in state.notice" :key="item.id" :name="'' + index">
            <template #title>
              <span style="font-size: 26px; font-weight: bold">{{ item.name }}</span>
              <span style="margin-left: 10px; color: #888888">{{ item.time }}</span>
            </template>
            <div class="editor-content-view" v-html="item.content" style=""></div>
          </el-collapse-item>
        </el-collapse>
      </el-card>
    </div>
  </div>
</template>