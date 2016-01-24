package com.madeinhk.model;

import android.os.Parcel;
import android.os.Parcelable;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.madeinhk.utils.ChineseUtils;

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
        String meaningString = ChineseUtils.convertChineseIfNeeded(lookupResult.getMeaning());
        String exampleString = ChineseUtils.convertChineseIfNeeded(lookupResult.getExample());
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

    private static final int MAX_LENGTH = 20;

    public String buildMeaningSummary() {
        StringBuilder sb = new StringBuilder();
        boolean isFirstItem = true;
        for (TypeEntry entry : mTypeEntry) {
            if (!isFirstItem) {
                sb.append(';');
            } else {
                isFirstItem = false;
            }

            sb.append(entry.mMeaning);
            if (sb.length() > MAX_LENGTH) {
                break;
            }
        }
        return ChineseUtils.convertChineseIfNeeded(sb.toString());
    }


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
            String description;
            switch (mType) {
                case 0:
                    description = "abbr. 縮寫";
                    break;
                case 1:
                    description = "a. 形容詞";
                    break;
                case 2:
                    description = "int. 感嘆詞";
                    break;
                case 3:
                    description = "n. 名詞";
                    break;
                case 4:
                    description = "pr n";
                    break;
                case 5:
                    description = "ad. 副詞";
                    break;
                case 6:
                    description = "vt";
                    break;
                case 7:
                    description = "vi";
                    break;
                case 8:
                    description = "npl";
                    break;
                case 9:
                    description = "";
                    break;
                case 10:
                    description = "abbr";
                    break;
                case 11:
                    description = "pt";
                    break;
                case 12:
                    description = "vt. 及物動詞";
                    break;
                case 13:
                    description = "excl";
                    break;
                case 14:
                    description = "prep. 介係詞";
                    break;
                case 15:
                    description = "prep phr";
                    break;
                case 16:
                    description = "conj phr";
                    break;
                case 17:
                    description = "vi. 不及物動詞";
                    break;
                case 18:
                    description = "n.[theS]";
                    break;
                case 19:
                    description = "pref.";
                    break;
                case 20:
                    description = "n.【農】";
                    break;
                case 21:
                    description = "pt pp";
                    break;
                case 22:
                    description = "vi. & vt.";
                    break;
                case 23:
                    description = "n. [C] 可數名詞";
                    break;
                case 24:
                    description = "pron. 代名詞";
                    break;
                case 25:
                    description = "v refl";
                    break;
                case 26:
                    description = "comb. form";
                    break;
                case 27:
                    description = "modif";
                    break;
                case 28:
                    description = "suf. 字尾";
                    break;
                case 29:
                    description = "adv phr";
                    break;
                case 30:
                    description = "advs";
                    break;
                case 31:
                    description = "n.[U]【律】";
                    break;
                case 32:
                    description = "ns";
                    break;
                case 33:
                    description = "adjs";
                    break;
                case 34:
                    description = "n.【英】【俚】";
                    break;
                case 35:
                    description = "n. & a.";
                    break;
                case 36:
                    description = "suf.(構成形容詞或名詞)";
                    break;
                case 37:
                    description = "v.";
                    break;
                case 38:
                    description = "comb. form(構成名詞)";
                    break;
                case 39:
                    description = "adj n";
                    break;
                case 40:
                    description = "pp";
                    break;
                case 41:
                    description = "n.【化】";
                    break;
                case 42:
                    description = "ph. 片語";
                    break;
                case 43:
                    description = "abbrs";
                    break;
                case 44:
                    description = "det";
                    break;
                case 45:
                    description = "combining form";
                    break;
                case 46:
                    description = "a.[F]";
                    break;
                case 47:
                    description = "a.【植】";
                    break;
                case 48:
                    description = "adj adv";
                    break;
                case 49:
                    description = "a.[Z]";
                    break;
                case 50:
                    description = "suf.(構成名詞)";
                    break;
                case 51:
                    description = "excls";
                    break;
                case 52:
                    description = "n.(複數)";
                    break;
                case 53:
                    description = "n.【建】";
                    break;
                case 54:
                    description = "adj phr";
                    break;
                case 55:
                    description = "suf.(名詞字尾)";
                    break;
                case 56:
                    description = "a.【口】";
                    break;
                case 57:
                    description = "n.[P1]";
                    break;
                case 58:
                    description = "modal aux";
                    break;
                case 59:
                    description = "a.【主蘇格蘭】";
                    break;
                case 60:
                    description = "n.【法】";
                    break;
                case 61:
                    description = "n.[U]";
                    break;
                case 62:
                    description = "a.【古】";
                    break;
                case 63:
                    description = "suf.(形容詞字尾)";
                    break;
                case 64:
                    description = "ad.【古】";
                    break;
                case 65:
                    description = "n.【古】";
                    break;
                case 66:
                    description = "vt vi";
                    break;
                case 67:
                    description = "n.【植】";
                    break;
                case 68:
                    description = "a. & ad.";
                    break;
                case 69:
                    description = "suf.(構成形容詞)";
                    break;
                case 70:
                    description = "suf.(形容詞語尾)";
                    break;
                case 71:
                    description = "suf.(形成人物之意的名詞或形容詞詞尾)";
                    break;
                case 72:
                    description = "vi vt";
                    break;
                case 73:
                    description = "suf.(附在動詞之後形成名詞)";
                    break;
                case 74:
                    description = "n vt";
                    break;
                case 75:
                    description = "comb. form(構成形容詞)";
                    break;
                case 76:
                    description = "n.【解】";
                    break;
                case 77:
                    description = "n.【動】";
                    break;
                case 78:
                    description = "a.【動】";
                    break;
                case 79:
                    description = "n.【美】";
                    break;
                case 80:
                    description = "n.【主英】";
                    break;
                case 81:
                    description = "n.【南非】";
                    break;
                case 82:
                    description = "n.(複數，用作單數)";
                    break;
                case 83:
                    description = "conj";
                    break;
                case 84:
                    description = "n.【俚】";
                    break;
                case 85:
                    description = "pres p";
                    break;
                case 86:
                    description = "n.【空】";
                    break;
                case 87:
                    description = "a.[B]";
                    break;
                case 88:
                    description = "phr";
                    break;
                case 89:
                    description = "a.【美】【俚】";
                    break;
                case 90:
                    description = "n.【俚】【粗】";
                    break;
                case 91:
                    description = "n. & vi.";
                    break;
                case 92:
                    description = "a.【俚】";
                    break;
                case 93:
                    description = "vi.【古】";
                    break;
                case 94:
                    description = "particle";
                    break;
                case 95:
                    description = "vt.【法】";
                    break;
                case 96:
                    description = "n.(用作單數或複數)";
                    break;
                case 97:
                    description = "adv and prep phr";
                    break;
                case 98:
                    description = "pr npl";
                    break;
                case 99:
                    description = "a.【法】";
                    break;
                case 100:
                    description = "vt.【主英】";
                    break;
                case 101:
                    description = "n.【語】";
                    break;
                case 102:
                    description = "n.【蘇格蘭】【英】【方】";
                    break;
                case 103:
                    description = "conj. 連接詞";
                    break;
                case 104:
                    description = "n.(常用複數)";
                    break;
                case 105:
                    description = "n.【葡】";
                    break;
                case 106:
                    description = "n.【醫】";
                    break;
                case 107:
                    description = "a.[Z][B]";
                    break;
                case 108:
                    description = "n.【西】";
                    break;
                case 109:
                    description = "conj phr used after negative";
                    break;
                case 110:
                    description = "vt.【古】";
                    break;
                case 111:
                    description = "ad.【書】[(+as)]";
                    break;
                case 112:
                    description = "vi.【主英】";
                    break;
                case 113:
                    description = "a.【詩】";
                    break;
                case 114:
                    description = "n.【義】";
                    break;
                case 115:
                    description = "vi.【方】";
                    break;
                case 116:
                    description = "None";
                    break;
                case 117:
                    description = "n.【物】";
                    break;
                case 118:
                    description = "a.【宗】";
                    break;
                case 119:
                    description = "v impers";
                    break;
                case 120:
                    description = "n.【英】【口】";
                    break;
                case 121:
                    description = "n.【蘇格蘭】";
                    break;
                case 122:
                    description = "vt.【化】";
                    break;
                case 123:
                    description = "a. & int.";
                    break;
                case 124:
                    description = "vt.【英】【方】";
                    break;
                case 125:
                    description = "vi.【英】【方】";
                    break;
                case 126:
                    description = "n.【英】【方】";
                    break;
                case 127:
                    description = "ad. & prep. & conj.";
                    break;
                case 128:
                    description = "vt.【英】【俚】";
                    break;
                case 129:
                    description = "n.【口】";
                    break;
                case 130:
                    description = "vt.【口】";
                    break;
                case 131:
                    description = "n.[U]【美】";
                    break;
                case 132:
                    description = "ad.【蘇格蘭】";
                    break;
                case 133:
                    description = "n.【德】";
                    break;
                case 134:
                    description = "n.【生】";
                    break;
                case 135:
                    description = "n adj";
                    break;
                case 136:
                    description = "n.【電】";
                    break;
                case 137:
                    description = "n.【律】";
                    break;
                case 138:
                    description = "n.【罕】";
                    break;
                case 139:
                    description = "a.【醫】";
                    break;
                case 140:
                    description = "n.[C]【美】";
                    break;
                case 141:
                    description = "n.[C][(+on)]";
                    break;
                case 142:
                    description = "n.【蘇格蘭】【廢】";
                    break;
                case 143:
                    description = "n.[C]【貶】";
                    break;
                case 144:
                    description = "a. & ad.(常作表語)";
                    break;
                case 145:
                    description = "vt.【蘇格蘭】";
                    break;
                case 146:
                    description = "adv prep";
                    break;
                case 147:
                    description = "vt vi n";
                    break;
                case 148:
                    description = "a.【地】";
                    break;
                case 149:
                    description = "n excl";
                    break;
                case 150:
                    description = "aux. 助動詞";
                    break;
                case 151:
                    description = "int.【美】【俚】";
                    break;
                case 152:
                    description = "n.【澳】";
                    break;
                case 153:
                    description = "a.【澳】";
                    break;
                case 154:
                    description = "a.【罕】";
                    break;
                case 155:
                    description = "ad.【書】";
                    break;
                case 156:
                    description = "a.【解】";
                    break;
                case 157:
                    description = "n.【方】";
                    break;
                case 158:
                    description = "n. & vt.";
                    break;
                case 159:
                    description = "n.【拉】";
                    break;
                case 160:
                    description = "n.v.";
                    break;
                case 161:
                    description = "vt.[H]";
                    break;
                case 162:
                    description = "n.【商】";
                    break;
                case 163:
                    description = "n.【體】";
                    break;
                case 164:
                    description = "ad.【口】";
                    break;
                case 165:
                    description = "vt.【方】";
                    break;
                case 166:
                    description = "n.【俄】";
                    break;
                case 167:
                    description = "ad.【英】【口】";
                    break;
                case 168:
                    description = "n.[C]【法】";
                    break;
                case 169:
                    description = "n vi vt";
                    break;
                case 170:
                    description = "a.【義】【音】";
                    break;
                case 171:
                    description = "n.[U]【口】";
                    break;
                case 172:
                    description = "n.【地】";
                    break;
                case 173:
                    description = "n.[U][C]";
                    break;
                case 174:
                    description = "n.[U]【文】";
                    break;
                case 175:
                    description = "n.【美】【俚】";
                    break;
                case 176:
                    description = "vi n";
                    break;
                case 177:
                    description = "adv and pron phr";
                    break;
                case 178:
                    description = "n.[C]【古】";
                    break;
                case 179:
                    description = "n. & vi.【英】【方】";
                    break;
                case 180:
                    description = "n.[U]【古】";
                    break;
                case 181:
                    description = "n.【英】";
                    break;
                case 182:
                    description = "n.[U][S1]";
                    break;
                case 183:
                    description = "a.【英】【俚】";
                    break;
                case 184:
                    description = "a.(只作表語)";
                    break;
                case 185:
                    description = "n.【意】";
                    break;
                case 186:
                    description = "vt.【口】【謔】";
                    break;
                case 187:
                    description = "n.【魚】";
                    break;
                case 188:
                    description = "copula";
                    break;
                case 189:
                    description = "n.(用作單)";
                    break;
                case 190:
                    description = "n.[C][U]";
                    break;
                case 191:
                    description = "n.[C]【英】【史】";
                    break;
                case 192:
                    description = "v.aux.【古】";
                    break;
                case 193:
                    description = "n.【美】【口】";
                    break;
                case 194:
                    description = "n.【俚】【舊】";
                    break;
                case 195:
                    description = "a. & ad.【法】";
                    break;
                case 196:
                    description = "in adv phrs";
                    break;
                case 197:
                    description = "a.【方】";
                    break;
                case 198:
                    description = "n. & int.";
                    break;
                case 199:
                    description = "ad. & prep";
                    break;
                case 200:
                    description = "n.[the S]【古】【文】";
                    break;
                case 201:
                    description = "prep. & conj.";
                    break;
                case 202:
                    description = "n.【生化】";
                    break;
                case 203:
                    description = "a.【英】【方】";
                    break;
                case 204:
                    description = "vt.【英】";
                    break;
                case 205:
                    description = "int.【古】";
                    break;
                case 206:
                    description = "pref.(用在b m p前)";
                    break;
                case 207:
                    description = "n.【印英】";
                    break;
                case 208:
                    description = "n.【生】【物】【化】";
                    break;
                case 209:
                    description = "a.(用作表語)";
                    break;
                case 210:
                    description = "pt pp";
                    break;
                case 211:
                    description = "n.(複數，亦作microelectronics)";
                    break;
                case 212:
                    description = "n.【心】";
                    break;
                case 213:
                    description = "adv phr pron phr";
                    break;
                case 214:
                    description = "vi.【美】";
                    break;
                case 215:
                    description = "a.【海】";
                    break;
                case 216:
                    description = "a.【生】";
                    break;
                case 217:
                    description = "excl adj adv vt n";
                    break;
                case 218:
                    description = "excl adj adv";
                    break;
                case 219:
                    description = "a.【英方】";
                    break;
                case 220:
                    description = "pref.(構成形容詞或副詞)";
                    break;
                case 221:
                    description = "n. & vi. & vt.";
                    break;
                case 222:
                    description = "n. & v.";
                    break;
                case 223:
                    description = "n.【冶】";
                    break;
                case 224:
                    description = "n vi";
                    break;
                case 225:
                    description = "a.【物】";
                    break;
                case 226:
                    description = "n.【哲】";
                    break;
                case 227:
                    description = "a.[U]";
                    break;
                case 228:
                    description = "adj n vt vi";
                    break;
                case 229:
                    description = "pl";
                    break;
                case 230:
                    description = "n.[J]";
                    break;
                case 231:
                    description = "a.(常作表語)";
                    break;
                case 232:
                    description = "n.[M][G]";
                    break;
                case 233:
                    description = "vt.【美】【口】";
                    break;
                case 234:
                    description = "vi.【口】";
                    break;
                case 235:
                    description = "a.【美】【口】";
                    break;
                case 236:
                    description = "n.【拉】【書】";
                    break;
                case 237:
                    description = "n.【棒】";
                    break;
                case 238:
                    description = "vt.【海】";
                    break;
                case 239:
                    description = "vt.【主英】[W]";
                    break;
                case 240:
                    description = "adj adv particle n";
                    break;
                case 241:
                    description = "a.【數】";
                    break;
                case 242:
                    description = "n.【數】";
                    break;
                case 243:
                    description = "n.【美】【加】";
                    break;
                case 244:
                    description = "a.【美】(= 【英】gray-headed)";
                    break;
                case 245:
                    description = "vt n";
                    break;
                case 246:
                    description = "a.【美】";
                    break;
                case 247:
                    description = "vt.【古】【詩】";
                    break;
                case 248:
                    description = "abbr.【電腦】";
                    break;
                case 249:
                    description = "n.【英】【史】";
                    break;
                case 250:
                    description = "n.[K]";
                    break;
                case 251:
                    description = "a.【主英】";
                    break;
                case 252:
                    description = "a.【昆】";
                    break;
                case 253:
                    description = "pref.【化】";
                    break;
                case 254:
                    description = "n.[M]";
                    break;
                case 255:
                    description = "n.【美】【軍】";
                    break;
                case 256:
                    description = "vi.【文】(用於否定句、疑問句)";
                    break;
                case 257:
                    description = "n.[K]【主美】";
                    break;
                case 258:
                    description = "a.【古】[F]";
                    break;
                case 259:
                    description = "n.[C]【書】";
                    break;
                case 260:
                    description = "prep.【古】【詩】";
                    break;
                case 261:
                    description = "n.[theS][M][G]";
                    break;
                case 262:
                    description = "n.(somersault之變體)";
                    break;
                case 263:
                    description = "n.【音】";
                    break;
                case 264:
                    description = "vt.【美】【俚】";
                    break;
                case 265:
                    description = "adv pron";
                    break;
                case 266:
                    description = "n.【愛爾蘭】";
                    break;
                case 267:
                    description = "n.【蘇格蘭】(用作單或複)";
                    break;
                case 268:
                    description = "pron phr";
                    break;
                case 269:
                    description = "n.[C]【植】";
                    break;
                case 270:
                    description = "n.(亦作permayouth)";
                    break;
                default:
                    description = "N/A";
            }
            return ChineseUtils.convertChineseIfNeeded(description);
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
