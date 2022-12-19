package ctrmap.util.tools.qos;

import xstandard.io.base.impl.ext.data.DataIOStream;
import java.io.IOException;

public class QOSInstructions {

	public static Base read(DataIOStream in) throws IOException {
		in.checkpoint();
		int first = in.readUnsignedByte();
		QOSOpCode opCode = QOSOpCode.values()[first & 0x1F];
		in.resetCheckpoint();
		System.out.println("Reading instruction " + opCode + "(0x" + Integer.toHexString(first) + ") at 0x" + Integer.toHexString(in.getPosition()));

		Base ins = null;

		switch (opCode) {
			case ADD:
			case AND:
			case B:
			case BAND:
			case BNEG:
			case BNZ:
			case BOR:
			case BXOR:
			case BZER:
			case CMPEQ:
			case CMPNE:
			case CMPGT:
			case CMPGE:
			case CMPLE:
			case CMPLT:
			case DIV:
			case LSH:
			case RSH:
			case MOD:
			case MUL:
			case NEG:
			case OR:
			case PUSH:
			case SUB:
			case NOT:
				ins = new CommonOp(in);
				break;
			case CALL:
				ins = new Call(in);
				break;
			case NOP:
				ins = new Nop(in);
				break;
			case POP:
				ins = new Pop(in);
				break;
			case PUSHSP:
				ins = new PushSp(in);
				break;
			case PUSHSP_:
				ins = new PushSp_(in);
				break;
			case RET:
				ins = new ReturnVoid(in);
				break;
			case RETV:
				ins = new ReturnValue(in);
				break;
		}

		return ins;
	}

	public static class Base {

		public int addr;
		public QOSOpCode opCode;
		public int arg;

		public Base(DataIOStream in) throws IOException {
			addr = in.getPosition();
			int first = in.readUnsignedByte();
			opCode = QOSOpCode.values()[first & 0x1F];
			arg = first >>> 5;
		}

		public String dump(QOSDecompiler.QOSScript script) {
			return opCode + "(" + arg + ")";
		}
	}

	public static class Nop extends Base {

		public Nop(DataIOStream in) throws IOException {
			super(in);
		}
	}

	public static class CommonOp extends Base {

		public OperandType operandType;
		public int operand;

		public CommonOp(DataIOStream in) throws IOException {
			super(in);
			operandType = OperandType.values()[arg];
			switch (operandType) {
				case STACK:
					operand = -1;
					break;
				case CONST8:
					operand = in.readUnsignedByte();
					break;
				case CONST16:
					operand = in.readUnsignedShort();
					break;
				case CONST32:
					operand = in.readInt();
					break;
				case LOCAL:
					operand = in.readByte();
					break;
				case GLOBAL:
				case FUNCTION:
					operand = in.readUnsignedByte();
					break;
				case ADDRESS:
					operand = in.readUnsignedShort();
					break;
			}
		}

		@Override
		public String dump(QOSDecompiler.QOSScript script) {
			StringBuilder sb = new StringBuilder(opCode.toString());
			sb.append("(");
			switch (operandType) {
				case STACK:
					sb.append("_pop");
					break;
				case CONST8:
				case CONST16:
				case CONST32:
					sb.append(operand);
					break;
				case LOCAL:
					sb.append("loc_");
					sb.append(operand);
					break;
				case GLOBAL:
					sb.append("glob_");
					sb.append(operand);
					break;
				case FUNCTION:
					sb.append(script.imports[operand].name);
					break;
				case ADDRESS:
					if (script.strings.containsKey(operand)) {
						sb.append('"').append(script.strings.get(operand)).append('"');
					} else {
						sb.append("&off_");
						sb.append(Integer.toHexString(operand));
					}
					break;
			}
			sb.append(")");
			return sb.toString();
		}

		public static enum OperandType {
			STACK, //0
			CONST8,//1
			CONST16,//2
			CONST32,//3
			LOCAL,//4
			GLOBAL,//5
			FUNCTION,//6
			ADDRESS//7
		}
	}

	public static class Call extends Base {

		public Type type;
		public Target target;

		public Call(DataIOStream in) throws IOException {
			super(in);
			type = Type.values()[arg];

			switch (type) {
				case LOCAL:
					target = new LocalTarget(in);
					break;
				case NATIVE:
					target = new NativeTarget(in);
					break;
				default:
					target = null;
					break;
			}
		}

		@Override
		public String dump(QOSDecompiler.QOSScript script) {
			switch (type) {
				default:
					return "NULL_CALL()";
				case LOCAL:
					return "sub_" + Integer.toHexString(((LocalTarget) target).address) + "()";
				case NATIVE:
					return script.imports[((NativeTarget) target).index].name + "()";
				case STACK:
					return "STACK_CALL()";
			}
		}

		public interface Target {
		}

		public static class LocalTarget implements Target {

			public int address;

			public LocalTarget(DataIOStream in) throws IOException {
				address = in.readUnsignedShort();
			}
		}

		public static class NativeTarget implements Target {

			public int index;

			public NativeTarget(DataIOStream in) throws IOException {
				index = in.readUnsignedByte();
			}
		}

		public static enum Type {
			STACK,
			DMY1,
			DMY2,
			DMY3,
			DMY4,
			DMY5,
			NATIVE,
			LOCAL
		}
	}

	public static class ReturnVoid extends Base {

		public int stackAdjustSize;

		public ReturnVoid(DataIOStream in) throws IOException {
			super(in);
			stackAdjustSize = arg == 1 ? in.readUnsignedByte() : 0;
		}

		@Override
		public String dump(QOSDecompiler.QOSScript script) {
			return "RETURN(" + stackAdjustSize + ")";
		}
	}

	public static class ReturnValue extends Base {

		public int stackAdjustSize;

		public ReturnValue(DataIOStream in) throws IOException {
			super(in);
			stackAdjustSize = arg == 1 ? in.readUnsignedByte() : 0;
		}

		@Override
		public String dump(QOSDecompiler.QOSScript script) {
			return "RETURNVAL(" + stackAdjustSize + ")";
		}
	}

	public static class Pop extends Base {

		public Type type;
		public int index;

		public Pop(DataIOStream in) throws IOException {
			super(in);
			type = Type.values()[arg];
			switch (type) {
				case TO_LOCAL:
				case TO_FUNCTION:
				case TO_GLOBAL:
					index = in.readUnsignedByte();
					break;
				default:
					index = -1;
					break;
			}
		}

		@Override
		public String dump(QOSDecompiler.QOSScript script) {
			switch (type) {
				case TO_LOCAL:
					return "POP(loc_" + index + ")";
				case TO_GLOBAL:
					return "POP(glob_" + index + ")";
				case TO_FUNCTION:
					return "POP(" + script.imports[index].name + ")";
				default:
					return "POP()";
			}
		}

		public static enum Type {
			DISCARD,
			DMY1,
			DMY2,
			DMY3,
			TO_LOCAL,
			TO_GLOBAL,
			TO_FUNCTION
		}
	}

	public static class PushSp_ extends Base {

		public PushSp.Subject subject;

		public PushSp_(DataIOStream in) throws IOException {
			super(in);
			if (arg != 0) {
				if (in.readUnsignedByte() == 1) {
					subject = PushSp.Subject.SCRIPTING_ENGINE;
				}
			}
		}

		@Override
		public String dump(QOSDecompiler.QOSScript script) {
			return "PUSHSP_(" + subject + ")";
		}
	}

	public static class PushSp extends Base {

		public Subject subject;

		public PushSp(DataIOStream in) throws IOException {
			super(in);
			if (in.readUnsignedByte() == 1) {
				subject = Subject.SCRIPTING_ENGINE;
			}
		}

		@Override
		public String dump(QOSDecompiler.QOSScript script) {
			return "PUSHSP(" + subject + ")";
		}

		public enum Subject {
			SCRIPTING_ENGINE
		}
	}
}
