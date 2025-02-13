<script setup>
import {nextTick, reactive, defineEmits, ref} from "vue";
import request from "@/utils/request";
import {ElMessage} from "element-plus";
import config from "../../config";
import {useUserStore} from "@/stores/user";


const userStore = useUserStore()
const user = userStore.getUser
const token = userStore.getBearerToken

const state = reactive({
  form: {}
})
state.form = Object.assign({}, user)
const handleImportSuccess = (res) => {
  state.form.avatar = res.data
  ElMessage.success("上传头像成功")
}
const load = () => {
  request.get('/user/' + user.id).then(res => {
    state.form = res.data
  })
}
load()

const rules = reactive({
  name: [
    { required: true, message: '请输入昵称', trigger: 'blur' },
    { min: 2, max: 20, message: '长度在2-20之间', trigger: 'blur' },
  ],
  address: [
    { min: 3, max: 50, message: '长度在3-50之间', trigger: 'blur' },
  ],
  school: [
    { min: 3, max: 30, message: '长度在3-30之间', trigger: 'blur' },
  ],
  profile: [
    { max: 100, message: '长度小于100', trigger: 'blur' },
  ],

})
const ruleFormRef = ref()

let $myEmit = defineEmits(['getAvatar'])
// 保存
const save = () => {
  ruleFormRef.value.validate(valid => {   // valid就是校验的结果
    if (valid) {
      request.put("/updateUser", state.form).then(res => {
        if (res.code === '200') {
          ElMessage.success('更新成功')
          userStore.setUser(res.data)
          $myEmit('getAvatar', res.data.avatar)
          load()
        } else {
          ElMessage.error(res.msg)
        }
      })
    }
  })
}



</script>

<template>
  <div style="width: 50%; margin: auto;">
    <el-card style="min-height: calc(100vh - 100px)">
      <div style="font-size: 20px; font-weight: bold; color: orange; margin: 20px 0 0 30px">
        个人信息
      </div>
      <el-form style="width: 60%; margin: 0 auto" label-width="100px" ref="ruleFormRef" :rules="rules" :model="state.form" status-icon>
        <div style="text-align: center">
          <el-upload
              class="avatar-uploader"
              :show-file-list="false"
              :action='`http://${config.serverUrl}/file/upload`'
              :on-success="handleImportSuccess"
              :headers="{ Authorization: token}"
          >
            <img v-if="state.form.avatar" :src="state.form.avatar" class="avatar" />
            <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
          </el-upload>
        </div>

        <el-form-item label="用户名" style="margin-top: 20px" prop="username">
          <el-input v-model="state.form.username" disabled></el-input>
        </el-form-item>
        <el-form-item label="邮箱" prop="email">
          <el-input v-model="state.form.email" disabled></el-input>
        </el-form-item>
        <el-form-item label="积分" prop="score">
          <el-input v-model="state.form.score" disabled></el-input>
        </el-form-item>
        <el-form-item label="昵称" prop="name">
          <el-input v-model="state.form.name"></el-input>
        </el-form-item>
        <el-form-item label="性别" prop="sex">
          <el-radio-group v-model="state.form.sex">
            <el-radio label="男">男</el-radio>
            <el-radio label="女">女</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="出生日期" prop="birthday">
          <el-date-picker style="width: 100%" v-model="state.form.birthday" type="date" value-format="YYYY-MM-DD" placeholder="选择日期"></el-date-picker>
        </el-form-item>
        <el-form-item label="学校" prop="school">
          <el-input v-model="state.form.school"></el-input>
        </el-form-item>
        <el-form-item label="地址" prop="address">
          <el-input v-model="state.form.address"></el-input>
        </el-form-item>
        <el-form-item label="个人简介" prop="profile">
          <el-input v-model="state.form.profile" type="textarea"></el-input>
        </el-form-item>


      </el-form>
      <div style="text-align: center; width: 100%">
        <el-button type="primary" @click="save">保存</el-button>
      </div>
    </el-card>
  </div>
</template>

<style>
.avatar-uploader .avatar {
  width: 150px;
  height: 150px;
  display: block;
}
.avatar-uploader .el-upload {
  border: 1px dashed #ccc;
  border-radius: 50%;
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.avatar-uploader .el-upload:hover {
  border-color: #ccc;
}

.el-icon.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 120px;
  height: 120px;
  text-align: center;
}
</style>

