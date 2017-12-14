package io.github.cvrunmin.toolbox

import android.app.*
import android.content.*
import org.jetbrains.anko.intentFor
import android.os.Binder
import android.support.v4.provider.DocumentFile
import io.github.cvrunmin.crmkjk.*
import java.io.ByteArrayOutputStream
import java.io.DataOutputStream
import java.lang.Byte.parseByte
import java.util.*


class CryptImagesService : IntentService("CryptImagesService") {

    override fun onHandleIntent(intent: Intent?) {
        var intent1 = intentFor<BroadCaster>("cancel" to true)
        var noticeMgr = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        var nb = Notification.Builder(this)
        nb.setSmallIcon(android.R.drawable.stat_notify_more);
        nb.setCategory(Notification.CATEGORY_PROGRESS);
        //nb.AddAction(new Notification.Action())
        nb.setOngoing(true);
        //string dir = intent.GetStringExtra("dir");
        var dir = intent?.getParcelableExtra("dir") as android.net.Uri;
        var cd = intent?.getParcelableExtra("files") as ClipData
        var decrypt = intent?.getBooleanExtra("decrypt",false)

        intent1.putExtra("noticeId", 0x731);
        intent1.putExtra("decrypt", decrypt);
        var pi = PendingIntent.getBroadcast(this, 0, intent1, 0)

        nb.setContentTitle(String.format("Images %s", if(decrypt) "decrypting" else "encrypting"));
        nb.setContentText("Getting file list...");
        var action = Notification.Action(android.R.drawable.ic_delete, "Cancel", pi)
        //nb.AddAction(action);
        noticeMgr.notify(0x731, nb.build())
        var files = mutableListOf<DocumentFile>()
        if (dir != null)
        {
            files = DocumentFile.fromTreeUri(this, dir).listFiles().toMutableList();
        }
        else if (cd != null) {
            for (i in 0..(cd.itemCount - 1))
            {
                files.add(DocumentFile.fromSingleUri(this, cd.getItemAt(i).uri))
            }
        }
        noticeMgr.notify(0x731, nb.setContentText(String.format("doing job... (0/%d)", files.size)).setProgress(files.size, 0, false).build());

        var (origin, encoded) =genUniversalKeyPair(Random())
        for (i in files.indices)
        {
            var b = "";
            try
            {
                contentResolver.openInputStream(files[i].uri).use {
                    if(!decrypt){
                        var a = ByteArrayOutputStream()
                        it.copyTo(a)
                        var by = a.toByteArray()
                        var sb = StringBuilder()
                        for(item in by){
                            sb.append("%02x".format(item))
                        }
                        b = sb.toString()
                    }else{
                        b = it.reader().readText()
                    }
                }

                if (decrypt)
                {
                    b = decode(b)
                }
                else
                {
                    b = encode(b, origin, encoded, Charsets.UTF_8, state =  ENCODE_TEXT_BASE64_ENCODE);
                }
                contentResolver.openOutputStream(files[i].uri).use {
                    if(decrypt){
                        DataOutputStream(it).use {
                            var by = ByteArray(b.length / 2)
                            for (i1 in by.indices){
                                try {
                                    by[i1] = parseByte(b.substring(i1 * 2, i1 * 2 + 2), 16)
                                }catch (e : Exception){
                                    continue
                                }
                            }
                            it.write(by)
                        }
                    }else{
                        it.writer(Charsets.UTF_8).write(b)
                    }
                }
            }
            catch (e : Exception) {
                android.util.Log.w("Toolbox", e)
            }
            finally
            {
                b = "";
                noticeMgr.notify(0x731, nb.setContentText(String.format("doing job... (%d/%d)", i + 1, files.size)).setProgress(files.size, i + 1, false).build());
            }

        }
        noticeMgr.notify(0x731, nb.setContentText("done!").setOngoing(false).setProgress(0, 0, false).build());
    }

    class ServiceBinder(s : CryptImagesService) : Binder() {
        var service = s
    }
    class BroadCaster : BroadcastReceiver() {

        override fun onReceive(context: Context, intent: Intent) {
            val requireCancel = intent.getBooleanExtra("cancel", false)
            if (requireCancel) context.stopService(Intent(context, CryptImagesService::class.java))
            val noticeId = intent.getIntExtra("noticeId", 0x731)
            val noticeMgr = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            val nb = Notification.Builder(context)
            nb.setSmallIcon(android.R.drawable.stat_notify_more)
            nb.setCategory(Notification.CATEGORY_PROGRESS)
            val decrypt = intent.getBooleanExtra("decrypt", false)
            nb.setContentTitle(String.format("Images %s", if (decrypt) "decrypting" else "encrypting"))
            nb.setContentText("Progress cancelled")
            noticeMgr.notify(noticeId, nb.build())
        }
    }

}
