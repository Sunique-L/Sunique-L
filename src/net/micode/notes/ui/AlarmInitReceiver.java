/*
 * Copyright (c) 2010-2011, The MiCode Open Source Community (www.micode.net)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package net.micode.notes.ui;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;

import net.micode.notes.data.Notes;
import net.micode.notes.data.Notes.NoteColumns;

/*这个广播接收器的主要功能是设置一个闹钟，
闹钟在每条笔记的 NoteColumns.ALERTED_DATE 到达时触发，并通过广播将笔记的 ID 发送给 AlarmReceiver 类进行处理。*/

// 定义一个名为 AlarmInitReceiver 的公共类，该类继承自 BroadcastReceiver 类  
public class AlarmInitReceiver extends BroadcastReceiver {  
  
    // 定义一个静态的字符串数组，其中包含两个字符串元素：ID 和 AlteredDate  
    private static final String [] PROJECTION = new String [] {  
        NoteColumns.ID,  
        NoteColumns.ALERTED_DATE  
    };  
  
    // 定义两个静态整数常量，用于在后续代码中作为索引使用，分别为 PROJECTION 数组中的 ID 和 AlteredDate 元素的索引  
    private static final int COLUMN_ID                = 0;  
    private static final int COLUMN_ALERTED_DATE      = 1;  
  
    // 重写 BroadcastReceiver 类的 onReceive 方法，该方法在接收到 Intent 时被调用  
    @Override  
    public void onReceive(Context context, Intent intent) {  
        // 获取当前的系统时间，以毫秒为单位  
        long currentDate = System.currentTimeMillis();  
        // 使用当前 Context 和定义的 projection 对数据进行查询，查询条件为：AlteredDate 大于当前时间（currentDate）并且 Type 等于 NOTE_TYPE  
        Cursor c = context.getContentResolver().query(Notes.CONTENT_NOTE_URI,  
                PROJECTION,  
                NoteColumns.ALERTED_DATE + ">? AND " + NoteColumns.TYPE + "=" + Notes.TYPE_NOTE,  
                new String[] { String.valueOf(currentDate) },  
                null);  
  
        // 检查查询结果是否为空，如果不为空则进行后续操作  
        if (c != null) {  
            // 将查询结果移动到第一条数据，如果存在的话  
            if (c.moveToFirst()) {  
                // 进行循环操作，直到遍历完所有的查询结果  
                do {  
                    // 获取查询结果中 AlteredDate 字段的值  
                    long alertDate = c.getLong(COLUMN_ALERTED_DATE);  
                    // 创建一个新的 Intent，目标为 AlarmReceiver 类  
                    Intent sender = new Intent(context, AlarmReceiver.class);  
                    // 将查询结果中 Id 字段的值设置为 Intent 的数据，以便在 AlarmReceiver 中使用  
                    sender.setData(ContentUris.withAppendedId(Notes.CONTENT_NOTE_URI, c.getLong(COLUMN_ID)));  
                    // 创建一个新的 PendingIntent，用于稍后执行，并且目标为当前的 Intent 和广播类型  
                    PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, sender, 0);  
                    // 从 Context 中获取系统的 AlarmManager 服务  
                    AlarmManager alermManager = (AlarmManager) context  
                            .getSystemService(Context.ALARM_SERVICE);  
                    // 使用 AlarmManager 设置一个新的闹钟，在 alertDate 时间触发，使用上面创建的 PendingIntent 来发送广播唤醒应用  
                    alermManager.set(AlarmManager.RTC_WAKEUP, alertDate, pendingIntent);  
                } while (c.moveToNext());  // 移动到下一条数据，如果存在的话，重复上述操作直到遍历完所有数据。  
            }  
            c.close();  // 关闭查询结果集。  
        }  
    }  
}