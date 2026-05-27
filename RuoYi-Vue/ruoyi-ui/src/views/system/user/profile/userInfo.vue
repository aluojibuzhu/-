<template>
  <el-form ref="form" class="profile-edit-form" :model="form" :rules="rules" label-width="92px">
    <el-row :gutter="18">
      <el-col :span="12" :xs="24">
        <el-form-item label="用户昵称" prop="nickName">
          <el-input v-model="form.nickName" maxlength="30" placeholder="请输入用户昵称" />
        </el-form-item>
      </el-col>
      <el-col :span="12" :xs="24">
        <el-form-item label="手机号码" prop="phonenumber">
          <el-input v-model="form.phonenumber" maxlength="11" placeholder="请输入手机号码" />
        </el-form-item>
      </el-col>
      <el-col :span="12" :xs="24">
        <el-form-item label="用户邮箱" prop="email">
          <el-input v-model="form.email" maxlength="50" placeholder="请输入用户邮箱" />
        </el-form-item>
      </el-col>
      <el-col :span="12" :xs="24">
        <el-form-item label="性别">
          <el-radio-group v-model="form.sex">
            <el-radio label="0">男</el-radio>
            <el-radio label="1">女</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-col>
    </el-row>
    <el-form-item class="form-actions">
      <el-button type="primary" size="small" icon="el-icon-check" @click="submit">保存</el-button>
      <el-button size="small" icon="el-icon-close" @click="close">关闭</el-button>
    </el-form-item>
  </el-form>
</template>

<script>
import { updateUserProfile } from '@/api/system/user'

export default {
  props: {
    user: {
      type: Object
    }
  },
  data() {
    return {
      form: {},
      rules: {
        nickName: [
          { required: true, message: '用户昵称不能为空', trigger: 'blur' }
        ],
        email: [
          { required: true, message: '用户邮箱不能为空', trigger: 'blur' },
          {
            type: 'email',
            message: '请输入正确的用户邮箱',
            trigger: ['blur', 'change']
          }
        ],
        phonenumber: [
          { required: true, message: '手机号码不能为空', trigger: 'blur' },
          {
            pattern: /^1[3|4|5|6|7|8|9][0-9]\d{8}$/,
            message: '请输入正确的手机号码',
            trigger: 'blur'
          }
        ]
      }
    }
  },
  watch: {
    user: {
      handler(user) {
        if (user) {
          this.form = { nickName: user.nickName, phonenumber: user.phonenumber, email: user.email, sex: user.sex }
        }
      },
      immediate: true
    }
  },
  methods: {
    submit() {
      this.$refs.form.validate(valid => {
        if (valid) {
          updateUserProfile(this.form).then(() => {
            this.$modal.msgSuccess('修改成功')
            this.user.nickName = this.form.nickName
            this.user.phonenumber = this.form.phonenumber
            this.user.email = this.form.email
            this.user.sex = this.form.sex
          })
        }
      })
    },
    close() {
      this.$tab.closePage()
    }
  }
}
</script>

<style lang="scss" scoped>
.profile-edit-form {
  padding-top: 10px;

  ::v-deep .el-form-item__label {
    color: #4e5969;
    font-weight: 600;
  }
}

.form-actions {
  margin-top: 8px;
}
</style>
