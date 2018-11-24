package com.example.login

import android.content.Intent
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.alibaba.android.arouter.facade.annotation.Autowired
import com.alibaba.android.arouter.facade.annotation.Route
import com.alibaba.android.arouter.launcher.ARouter
import com.example.common.Const
import com.example.common.Routers
import kotlinx.android.synthetic.main.login_activity_main.*

@Route(path = Routers.MUSIC_MAIN)
class LoginMainActivity : AppCompatActivity() {
    @Autowired
    @JvmField
    var username: String? = null

    @Autowired
    @JvmField
    var password: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ARouter.getInstance().inject(this)
        setContentView(R.layout.login_activity_main)
        login_username.setText(this.username)
        login_password.setText(this.password)

        login.setOnClickListener {
            val password = login_password.text.toString()
            if(!password.matches(Regex("\\d{6,}"))){
                Toast.makeText(this, "密码是6位以上纯数字", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            Toast.makeText(this, "登录成功!", Toast.LENGTH_SHORT).show()
            val int = Intent().putExtra(Const.Key.CODE, Const.Resp.SUCCESS)
                    .putExtra(Const.Key.MESSAGE, "登录成功! 欢迎${login_username.text}.")
            setResult(Const.Resp.SUCCESS, int)
            finish()
        }
    }
}
