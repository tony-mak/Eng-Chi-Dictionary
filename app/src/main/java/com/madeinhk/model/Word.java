package com.madeinhk.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by tony on 8/11/14.
 */
public class Word implements Parcelable {
    public String mWord;
    public String mPhoneticString;
    public List<TypeEntry> mTypeEntry;
    public int mDifficulty;

    public Word(String word, String phoneticString, List<TypeEntry> typeEntry, int difficulty) {
        this.mWord = word;
        this.mPhoneticString = phoneticString;
        this.mTypeEntry = typeEntry;
        this.mDifficulty = difficulty;
//        Collections.sort(mTypeEntry);
    }

    public static Word fromLookupResult(LookupResult lookupResult) {
        String vocab = lookupResult.getWord();
        Crashlytics.log("fromLookupResult: " + vocab);
        String mPhoneticString = lookupResult.getPhoneticString();
        String meaningString = lookupResult.getMeaning();
        String exampleString = lookupResult.getExample();
        int difficulty = lookupResult.getmDifficulty();
        List<TypeEntry> typeEntries = TypeEntry.parse(meaningString, exampleString);
        return new Word(vocab, mPhoneticString, typeEntries, difficulty);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(this.mWord);
        dest.writeString(this.mPhoneticString);
        dest.writeTypedList(mTypeEntry);
        dest.writeInt(mDifficulty);
    }

    private Word(Parcel in) {
        this.mWord = in.readString();
        this.mPhoneticString = in.readString();
        mTypeEntry = new ArrayList<>();
        in.readTypedList(mTypeEntry, TypeEntry.CREATOR);
        mDifficulty = in.readInt();
    }

    public static final Parcelable.Creator<Word> CREATOR = new Parcelable.Creator<Word>() {
        public Word createFromParcel(Parcel source) {
            return new Word(source);
        }

        public Word[] newArray(int size) {
            return new Word[size];
        }
    };

    public static class TypeEntry implements Parcelable, Comparable<TypeEntry> {
        public int mType;
        public String mEngExample;
        public String mChiExample;
        public String mMeaning;

        public static List<TypeEntry> parse(String meaningString, String exampleString) {
            List<TypeEntry> typeEntries = new ArrayList<>();
            String[] meaningTokens = meaningString.split("\\|");
            int length = meaningTokens.length / 2;
            String[] exampleTokens = exampleString.split("\\|", length * 2);
            for (int i = 0; i < length; i++) {
                int type = Integer.parseInt(meaningTokens[i * 2]);
                String meaning = meaningTokens[i * 2 + 1];
                TypeEntry entry = new TypeEntry();
                entry.mType = type;
                entry.mMeaning = meaning;
                entry.mEngExample = exampleTokens[i * 2];
                entry.mChiExample = exampleTokens[i * 2 + 1];
                typeEntries.add(entry);
            }
            return typeEntries;
        }

        public String getTypeDescription() {
            switch (mType) {
                case 0:
                    return "abbr. 縮寫";
                case 1:
                    return "a. 形容詞";
                case 2:
                    return "int. 感嘆詞";
                case 3:
                    return "n. 名詞";
                case 4:
                    return "pr n";
                case 5:
                    return "ad. 副詞";
                case 6:
                    return "vt";
                case 7:
                    return "vi";
                case 8:
                    return "npl";
                case 9:
                    return "";
                case 10:
                    return "abbr";
                case 11:
                    return "pt";
                case 12:
                    return "vt. 及物動詞";
                case 13:
                    return "excl";
                case 14:
                    return "prep. 介係詞";
                case 15:
                    return "prep phr";
                case 16:
                    return "conj phr";
                case 17:
                    return "vi. 不及物動詞";
                case 18:
                    return "n.[theS]";
                case 19:
                    return "pref.";
                case 20:
                    return "n.【農】";
                case 21:
                    return "pt pp";
                case 22:
                    return "vi. & vt.";
                case 23:
                    return "n. [C] 可數名詞";
                case 24:
                    return "pron. 代名詞";
                case 25:
                    return "v refl";
                case 26:
                    return "comb. form";
                case 27:
                    return "modif";
                case 28:
                    return "suf. 字尾";
                case 29:
                    return "adv phr";
                case 30:
                    return "advs";
                case 31:
                    return "n.[U]【律】";
                case 32:
                    return "ns";
                case 33:
                    return "adjs";
                case 34:
                    return "n.【英】【俚】";
                case 35:
                    return "n. & a.";
                case 36:
                    return "suf.(構成形容詞或名詞)";
                case 37:
                    return "v.";
                case 38:
                    return "comb. form(構成名詞)";
                case 39:
                    return "adj n";
                case 40:
                    return "pp";
                case 41:
                    return "n.【化】";
                case 42:
                    return "ph. 片語";
                case 43:
                    return "abbrs";
                case 44:
                    return "det";
                case 45:
                    return "combining form";
                case 46:
                    return "a.[F]";
                case 47:
                    return "a.【植】";
                case 48:
                    return "adj adv";
                case 49:
                    return "a.[Z]";
                case 50:
                    return "suf.(構成名詞)";
                case 51:
                    return "excls";
                case 52:
                    return "n.(複數)";
                case 53:
                    return "n.【建】";
                case 54:
                    return "adj phr";
                case 55:
                    return "suf.(名詞字尾)";
                case 56:
                    return "a.【口】";
                case 57:
                    return "n.[P1]";
                case 58:
                    return "modal aux";
                case 59:
                    return "a.【主蘇格蘭】";
                case 60:
                    return "n.【法】";
                case 61:
                    return "n.[U]";
                case 62:
                    return "a.【古】";
                case 63:
                    return "suf.(形容詞字尾)";
                case 64:
                    return "ad.【古】";
                case 65:
                    return "n.【古】";
                case 66:
                    return "vt vi";
                case 67:
                    return "n.【植】";
                case 68:
                    return "a. & ad.";
                case 69:
                    return "suf.(構成形容詞)";
                case 70:
                    return "suf.(形容詞語尾)";
                case 71:
                    return "suf.(形成人物之意的名詞或形容詞詞尾)";
                case 72:
                    return "vi vt";
                case 73:
                    return "suf.(附在動詞之後形成名詞)";
                case 74:
                    return "n vt";
                case 75:
                    return "comb. form(構成形容詞)";
                case 76:
                    return "n.【解】";
                case 77:
                    return "n.【動】";
                case 78:
                    return "a.【動】";
                case 79:
                    return "n.【美】";
                case 80:
                    return "n.【主英】";
                case 81:
                    return "n.【南非】";
                case 82:
                    return "n.(複數，用作單數)";
                case 83:
                    return "conj";
                case 84:
                    return "n.【俚】";
                case 85:
                    return "pres p";
                case 86:
                    return "n.【空】";
                case 87:
                    return "a.[B]";
                case 88:
                    return "phr";
                case 89:
                    return "a.【美】【俚】";
                case 90:
                    return "n.【俚】【粗】";
                case 91:
                    return "n. & vi.";
                case 92:
                    return "a.【俚】";
                case 93:
                    return "vi.【古】";
                case 94:
                    return "particle";
                case 95:
                    return "vt.【法】";
                case 96:
                    return "n.(用作單數或複數)";
                case 97:
                    return "adv and prep phr";
                case 98:
                    return "pr npl";
                case 99:
                    return "a.【法】";
                case 100:
                    return "vt.【主英】";
                case 101:
                    return "n.【語】";
                case 102:
                    return "n.【蘇格蘭】【英】【方】";
                case 103:
                    return "conj. 連接詞";
                case 104:
                    return "n.(常用複數)";
                case 105:
                    return "n.【葡】";
                case 106:
                    return "n.【醫】";
                case 107:
                    return "a.[Z][B]";
                case 108:
                    return "n.【西】";
                case 109:
                    return "conj phr used after negative";
                case 110:
                    return "vt.【古】";
                case 111:
                    return "ad.【書】[(+as)]";
                case 112:
                    return "vi.【主英】";
                case 113:
                    return "a.【詩】";
                case 114:
                    return "n.【義】";
                case 115:
                    return "vi.【方】";
                case 116:
                    return "None";
                case 117:
                    return "n.【物】";
                case 118:
                    return "a.【宗】";
                case 119:
                    return "v impers";
                case 120:
                    return "n.【英】【口】";
                case 121:
                    return "n.【蘇格蘭】";
                case 122:
                    return "vt.【化】";
                case 123:
                    return "a. & int.";
                case 124:
                    return "vt.【英】【方】";
                case 125:
                    return "vi.【英】【方】";
                case 126:
                    return "n.【英】【方】";
                case 127:
                    return "ad. & prep. & conj.";
                case 128:
                    return "vt.【英】【俚】";
                case 129:
                    return "n.【口】";
                case 130:
                    return "vt.【口】";
                case 131:
                    return "n.[U]【美】";
                case 132:
                    return "ad.【蘇格蘭】";
                case 133:
                    return "n.【德】";
                case 134:
                    return "n.【生】";
                case 135:
                    return "n adj";
                case 136:
                    return "n.【電】";
                case 137:
                    return "n.【律】";
                case 138:
                    return "n.【罕】";
                case 139:
                    return "a.【醫】";
                case 140:
                    return "n.[C]【美】";
                case 141:
                    return "n.[C][(+on)]";
                case 142:
                    return "n.【蘇格蘭】【廢】";
                case 143:
                    return "n.[C]【貶】";
                case 144:
                    return "a. & ad.(常作表語)";
                case 145:
                    return "vt.【蘇格蘭】";
                case 146:
                    return "adv prep";
                case 147:
                    return "vt vi n";
                case 148:
                    return "a.【地】";
                case 149:
                    return "n excl";
                case 150:
                    return "aux. 助動詞";
                case 151:
                    return "int.【美】【俚】";
                case 152:
                    return "n.【澳】";
                case 153:
                    return "a.【澳】";
                case 154:
                    return "a.【罕】";
                case 155:
                    return "ad.【書】";
                case 156:
                    return "a.【解】";
                case 157:
                    return "n.【方】";
                case 158:
                    return "n. & vt.";
                case 159:
                    return "n.【拉】";
                case 160:
                    return "n.v.";
                case 161:
                    return "vt.[H]";
                case 162:
                    return "n.【商】";
                case 163:
                    return "n.【體】";
                case 164:
                    return "ad.【口】";
                case 165:
                    return "vt.【方】";
                case 166:
                    return "n.【俄】";
                case 167:
                    return "ad.【英】【口】";
                case 168:
                    return "n.[C]【法】";
                case 169:
                    return "n vi vt";
                case 170:
                    return "a.【義】【音】";
                case 171:
                    return "n.[U]【口】";
                case 172:
                    return "n.【地】";
                case 173:
                    return "n.[U][C]";
                case 174:
                    return "n.[U]【文】";
                case 175:
                    return "n.【美】【俚】";
                case 176:
                    return "vi n";
                case 177:
                    return "adv and pron phr";
                case 178:
                    return "n.[C]【古】";
                case 179:
                    return "n. & vi.【英】【方】";
                case 180:
                    return "n.[U]【古】";
                case 181:
                    return "n.【英】";
                case 182:
                    return "n.[U][S1]";
                case 183:
                    return "a.【英】【俚】";
                case 184:
                    return "a.(只作表語)";
                case 185:
                    return "n.【意】";
                case 186:
                    return "vt.【口】【謔】";
                case 187:
                    return "n.【魚】";
                case 188:
                    return "copula";
                case 189:
                    return "n.(用作單)";
                case 190:
                    return "n.[C][U]";
                case 191:
                    return "n.[C]【英】【史】";
                case 192:
                    return "v.aux.【古】";
                case 193:
                    return "n.【美】【口】";
                case 194:
                    return "n.【俚】【舊】";
                case 195:
                    return "a. & ad.【法】";
                case 196:
                    return "in adv phrs";
                case 197:
                    return "a.【方】";
                case 198:
                    return "n. & int.";
                case 199:
                    return "ad. & prep";
                case 200:
                    return "n.[the S]【古】【文】";
                case 201:
                    return "prep. & conj.";
                case 202:
                    return "n.【生化】";
                case 203:
                    return "a.【英】【方】";
                case 204:
                    return "vt.【英】";
                case 205:
                    return "int.【古】";
                case 206:
                    return "pref.(用在b m p前)";
                case 207:
                    return "n.【印英】";
                case 208:
                    return "n.【生】【物】【化】";
                case 209:
                    return "a.(用作表語)";
                case 210:
                    return "pt pp";
                case 211:
                    return "n.(複數，亦作microelectronics)";
                case 212:
                    return "n.【心】";
                case 213:
                    return "adv phr pron phr";
                case 214:
                    return "vi.【美】";
                case 215:
                    return "a.【海】";
                case 216:
                    return "a.【生】";
                case 217:
                    return "excl adj adv vt n";
                case 218:
                    return "excl adj adv";
                case 219:
                    return "a.【英方】";
                case 220:
                    return "pref.(構成形容詞或副詞)";
                case 221:
                    return "n. & vi. & vt.";
                case 222:
                    return "n. & v.";
                case 223:
                    return "n.【冶】";
                case 224:
                    return "n vi";
                case 225:
                    return "a.【物】";
                case 226:
                    return "n.【哲】";
                case 227:
                    return "a.[U]";
                case 228:
                    return "adj n vt vi";
                case 229:
                    return "pl";
                case 230:
                    return "n.[J]";
                case 231:
                    return "a.(常作表語)";
                case 232:
                    return "n.[M][G]";
                case 233:
                    return "vt.【美】【口】";
                case 234:
                    return "vi.【口】";
                case 235:
                    return "a.【美】【口】";
                case 236:
                    return "n.【拉】【書】";
                case 237:
                    return "n.【棒】";
                case 238:
                    return "vt.【海】";
                case 239:
                    return "vt.【主英】[W]";
                case 240:
                    return "adj adv particle n";
                case 241:
                    return "a.【數】";
                case 242:
                    return "n.【數】";
                case 243:
                    return "n.【美】【加】";
                case 244:
                    return "a.【美】(= 【英】gray-headed)";
                case 245:
                    return "vt n";
                case 246:
                    return "a.【美】";
                case 247:
                    return "vt.【古】【詩】";
                case 248:
                    return "abbr.【電腦】";
                case 249:
                    return "n.【英】【史】";
                case 250:
                    return "n.[K]";
                case 251:
                    return "a.【主英】";
                case 252:
                    return "a.【昆】";
                case 253:
                    return "pref.【化】";
                case 254:
                    return "n.[M]";
                case 255:
                    return "n.【美】【軍】";
                case 256:
                    return "vi.【文】(用於否定句、疑問句)";
                case 257:
                    return "n.[K]【主美】";
                case 258:
                    return "a.【古】[F]";
                case 259:
                    return "n.[C]【書】";
                case 260:
                    return "prep.【古】【詩】";
                case 261:
                    return "n.[theS][M][G]";
                case 262:
                    return "n.(somersault之變體)";
                case 263:
                    return "n.【音】";
                case 264:
                    return "vt.【美】【俚】";
                case 265:
                    return "adv pron";
                case 266:
                    return "n.【愛爾蘭】";
                case 267:
                    return "n.【蘇格蘭】(用作單或複)";
                case 268:
                    return "pron phr";
                case 269:
                    return "n.[C]【植】";
                case 270:
                    return "n.(亦作permayouth)";
                default:
                    return "N/A";
            }
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(this.mMeaning);
            dest.writeInt(this.mType);
            dest.writeString(this.mEngExample);
            dest.writeString(this.mChiExample);
        }

        public TypeEntry() {
        }

        private TypeEntry(Parcel in) {
            this.mMeaning = in.readString();
            this.mType = (char) in.readInt();
            this.mEngExample = in.readString();
            this.mChiExample = in.readString();
        }

        public static final Parcelable.Creator<TypeEntry> CREATOR = new Parcelable
                .Creator<TypeEntry>() {
            public TypeEntry createFromParcel(Parcel source) {
                return new TypeEntry(source);
            }

            public TypeEntry[] newArray(int size) {
                return new TypeEntry[size];
            }
        };

        @Override
        public int compareTo(TypeEntry otherEntry) {
            // Assume the entry with example is more important
            boolean thisEntryHasExample = !TextUtils.isEmpty(mEngExample);
            boolean otherEntryHasExample = !TextUtils.isEmpty(otherEntry.mEngExample);
            if (thisEntryHasExample && !otherEntryHasExample) {
                return -1;
            }
            if (!thisEntryHasExample && otherEntryHasExample) {
                return 1;
            }
            // Assume the longer the description, the more important the entry is
            return -(this.mMeaning.length() - otherEntry.mMeaning.length());
        }
    }

}
