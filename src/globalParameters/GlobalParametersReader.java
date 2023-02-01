package globalParameters;


import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Properties;

public class GlobalParametersReader {

	/**
	 * Where the parameters are stored.
	 */
	private static Properties parameters;

	/**
	 * Private constructor..
	 */
	private GlobalParametersReader() {
		super();
	}

	public static void initialize(String configFilePath) {
		if (parameters == null) {
			FileInputStream fis = null;
			try {
				parameters = new Properties();
				fis = new FileInputStream(configFilePath);
				parameters.loadFromXML(fis);
			} catch (IOException ex) {
				throw new RuntimeException(
						"Error when reading " + configFilePath + " \n" + Arrays.toString(ex.getStackTrace()));
			} finally {
				if (fis != null) {
					try {
						fis.close();
					} catch (IOException e) {
						throw new RuntimeException(
								"Error when closing " + configFilePath + " \n" + Arrays.toString(e.getStackTrace()));
					}
				}
			}
		} else {
			throw new RuntimeException("The config file path is null");
		}
	}

	/**
	 * Gets the parameter associated with the given <code>name</code>.
	 * 
	 * @param name
	 *            the name of the parameter.
	 * @param <T>
	 *            the parameter type.
	 * @return <code>null</code> if the parameter has not been found / the value
	 *         of the parameter otherwise.
	 */
	@SuppressWarnings("unchecked")
	public static final <T> T get(final String name, final Class<T> c) {
		if (parameters.containsKey(name)) {
			try {
				Object o = parameters.get(name);
				switch (c.getName()) {
				case "java.lang.Integer":
					return (T) Integer.valueOf(o.toString());
				case "java.lang.Double":
					return (T) Double.valueOf(o.toString());
				case "java.lang.Boolean":
					return (T) Boolean.valueOf(o.toString());
				default:
					return (T) o;
				}
			} catch (ClassCastException ex) {
				ex.printStackTrace();
			}
		}
		throw new RuntimeException("Parameter not found : " + name);
	}

}
