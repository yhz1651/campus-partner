<script setup>
import router from "@/router";
import request from "@/utils/request";
import {onMounted, reactive, ref} from "vue";
import {useUserStore} from "@/stores/user";
const userStore = useUserStore()
const user = userStore.getUser

const id = router.currentRoute.value.query.id // 参数id
const state = reactive({
  infos: {},
})

const load = () => {
  request.get('/information/' + id).then(res => {
    state.infos = res.data
  })

}
onMounted(() => {
  load()
})
</script>

<template>
  <div style="width: 60%; margin: auto;">

    <el-card style="min-height: calc(100vh - 100px)">
      <div style="padding-bottom: 15px ;border-bottom: 1px solid #ddd; margin-bottom: 20px" v-if="state.infos">
        <div style="display: flex; flex-direction: column; margin-left: 10px">
          <!-- 标题  -->
          <span style="font-weight: bold; font-size: 30px">{{ state.infos.name }}</span>

          <div>
            <!-- 浏览数量  -->
            <el-icon style="top: 2px"><View/></el-icon>
            <span style="margin-left: 5px">{{ state.infos.view }}</span>

            <!-- 时间  -->
            <el-icon style="top: 2px; margin-left: 20px" ><Clock/></el-icon>
            <span style="margin-left: 5px">{{ state.infos.time }}</span>
          </div>
        </div>
      </div>

      <!-- 内容  -->
      <div class="editor-content-view" v-html="state.infos.content" style=""></div>

      <!-- 附件  -->
      <div style="margin: 50px 10px; display: flex; flex-direction: column; border-top: 1px solid #ddd; " v-if="state.infos.file">
        <div style="margin: 20px 0 10px 0">附件:</div>
        <div>
          <el-button type="primary" v-if="state.infos.file">
            <el-icon style="vertical-align: middle">
              <download/>
            </el-icon>
            <a :href="state.infos.file" target="_blank" style="text-decoration: none; color: white">
              <span style="vertical-align: middle; margin-left: 5px"> 点击下载 </span>
            </a>
          </el-button>
        </div>

      </div>

    </el-card>
  </div>
</template>