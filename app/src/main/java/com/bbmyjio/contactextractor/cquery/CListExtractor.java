package com.bbmyjio.contactextractor.cquery;

import android.content.Context;
import android.database.Cursor;
import android.provider.ContactsContract;
import android.util.Log;

import com.bbmyjio.contactextractor.i.IContactQuery;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import io.reactivex.Single;
import io.reactivex.SingleEmitter;
import io.reactivex.SingleOnSubscribe;

/**
 * Created by Nitesh on 25-04-2017.
 */

public class CListExtractor extends BaseContactListEx {
    private final static String TAG = CListExtractor.class.getSimpleName();

    private Context mContext;

    public CListExtractor(Context mContext) {
        super(mContext);
        this.mContext = mContext;
    }


    Single<List<CList>> getList(final int mFilterType, final String orderBy, final String limit, final String skip) {

        return Single.create(new SingleOnSubscribe<List<CList>>() {
            @Override
            public void subscribe(SingleEmitter<List<CList>> emitter) throws Exception {
                Cursor fetchCursor = getCursorByType(mFilterType);

                if (fetchCursor == null) {
                    emitter.onError(new Exception("Cursor is null"));
                    return;
                }


                Log.d(TAG, "|Start|" + new Date(System.currentTimeMillis()).toString() + "\n Cursor Count" + fetchCursor.getCount());
                int count = 0;

                List<CList> cLists = new ArrayList<>();

                Map<String, CList> cListMap = new HashMap<>();

                while (fetchCursor.moveToNext())
                    cListMap = fillMap(fetchCursor, cListMap, mFilterType);

                cLists.addAll(cListMap.values());
                cListMap.clear();

                Log.d(TAG, "|End" + new Date(System.currentTimeMillis()).toString() + "\n No Phone " + (fetchCursor.getCount() - count));

                fetchCursor.close();
                emitter.onSuccess(cLists);

            }
        });
    }

    private Map<String, CList> fillMap(Cursor fetchCursor, Map<String, CList> cListMap, int mFilterType) {

        String _id = null;
        String contactId = null;

        switch (mFilterType) {
            case IContactQuery.Filter.ONLY_NAME:
                _id = fetchCursor.getString(fetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName._ID));
                contactId = fetchCursor.getString(fetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredName.CONTACT_ID));
                break;

            case IContactQuery.Filter.ONLY_EMAIL:
                _id = fetchCursor.getString(fetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email._ID));
                contactId = fetchCursor.getString(fetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Email.CONTACT_ID));
                break;

            case IContactQuery.Filter.ONLY_PHONE:
                _id = fetchCursor.getString(fetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone._ID));
                contactId = fetchCursor.getString(fetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Phone.CONTACT_ID));
                break;

            case IContactQuery.Filter.ONLY_ACCOUNT:
                _id = fetchCursor.getString(fetchCursor.getColumnIndex(ContactsContract.RawContacts._ID));
                contactId = fetchCursor.getString(fetchCursor.getColumnIndex(ContactsContract.RawContacts.CONTACT_ID));
                break;

            case IContactQuery.Filter.ONLY_POSTCODE:
                _id = fetchCursor.getString(fetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal._ID));
                contactId = fetchCursor.getString(fetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.StructuredPostal.CONTACT_ID));
                break;

            case IContactQuery.Filter.ONLY_ORGANISATION:
                _id = fetchCursor.getString(fetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization._ID));
                contactId = fetchCursor.getString(fetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Organization.CONTACT_ID));
                break;

            case IContactQuery.Filter.ONLY_EVENTS:
                _id = fetchCursor.getString(fetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event._ID));
                contactId = fetchCursor.getString(fetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.Event.CONTACT_ID));
                break;
            case IContactQuery.Filter.ONLY_GROUPS:
                _id = fetchCursor.getString(fetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership._ID));
                contactId = fetchCursor.getString(fetchCursor.getColumnIndex(ContactsContract.CommonDataKinds.GroupMembership.CONTACT_ID));
                break;

            default:
                break;
        }

        CList cList;
        if (cListMap.containsKey(contactId))
            cList = cListMap.get(contactId);
        else {
            cList = new CList();
            cList.setId(_id);
            cList.setContactId(contactId);
        }

        IContactQuery iContactQuery = new BaseContactQuery(mContext, fetchCursor, cList, null);
        cList = queryFilterType(iContactQuery, mFilterType, cList);

        cListMap.put(contactId, cList);
        return cListMap;
    }

}
