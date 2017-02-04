/**
 * @author Robert Miki A00990619
 * @version
 */
package bgpay.voucher;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import bgpay.dao.Dao;
import bgpay.database.Database;
import bgpay.voucher.enumerations.VoucherFields;

public class VoucherDao extends Dao {

	public static final String TABLE_NAME = "vouchers";
	private static final Logger LOG = LogManager.getLogger();

	/**
	 * 
	 */
	public VoucherDao(Database database) {
		super(database, TABLE_NAME);
	}

	/**
	 * 
	 * @param voucher
	 * @throws SQLException
	 */
	public void add(Voucher voucher) throws SQLException {
		String sqlString = String.format("INSERT INTO %s values (?, ?, ?, ?, ?, ?)", TABLE_NAME);
		@SuppressWarnings("unused")
		boolean result = execute(sqlString, voucher.getProductionName(), voucher.getProductionCompany(), voucher.getRate(), voucher.getStartDateTime(),
				voucher.getEndDateTime(), voucher.getIsPaid());
		LOG.debug("Voucher added to database");
	}

	/**
	 * 
	 * @param voucher
	 * @throws SQLException
	 */
	public void update(Voucher voucher) throws SQLException {
		LOG.debug("Voucher " + voucher.getStartDateTime() + "updated" );
		String sqlString = String.format("UPDATE %s SET %s=?, %s=?, %s=?, %s=?, %s=?, %s=? WHERE %s=? AND %s=?", TABLE_NAME, //
				VoucherFields.PRODUCTION_NAME.getFieldTitle(), //
				VoucherFields.PRODUCTION_COMPANY.getFieldTitle(), //
				VoucherFields.PAY_RATE.getFieldTitle(), //
				VoucherFields.START_DATE_TIME.getFieldTitle(), //
				VoucherFields.END_DATE_TIME.getFieldTitle(), //
				VoucherFields.PAID.getFieldTitle(), //
				VoucherFields.START_DATE_TIME.getFieldTitle(), //
				VoucherFields.END_DATE_TIME.getFieldTitle()); //

		@SuppressWarnings("unused")
		boolean result = execute(sqlString, voucher.getProductionName(), voucher.getProductionCompany(), voucher.getRate(), voucher.getStartDateTime(),
				voucher.getEndDateTime(), voucher.getIsPaid(), voucher.getStartDateTime(), voucher.getEndDateTime());
	}

	/**
	 * 
	 * @param voucher
	 * @throws SQLException
	 */
	public void delete(Voucher voucher) throws SQLException {
		String sqlString = String.format("DELETE FROM %s WHERE %s=? AND %s=?", TABLE_NAME, VoucherFields.START_DATE_TIME.getFieldTitle(), VoucherFields.END_DATE_TIME.getFieldTitle());
		@SuppressWarnings("unused")
		boolean result = execute(sqlString, voucher.getStartDateTime(), voucher.getEndDateTime());
	}

	public Voucher getVoucher(LocalDate date) throws Exception {
		String sqlString = String.format("SELECT * FROM %s WHERE %s = %s", TABLE_NAME, VoucherFields.START_DATE_TIME.getFieldTitle(),
				date);

		Statement statement = null;
		ResultSet resultSet = null;

		try {
			Connection connection = Database.getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(sqlString);

			int count = 0;
			while (resultSet.next()) {
				if (count > 0)
					throw new Exception(String.format("Expected one result, got %d", count));
			}

			LocalDateTime startDateTime = resultSet.getTimestamp(VoucherFields.START_DATE_TIME.getFieldTitle()).toLocalDateTime();
			LocalDateTime endDateTime = resultSet.getTimestamp(VoucherFields.END_DATE_TIME.getFieldTitle()).toLocalDateTime();

			return new Voucher.Builder(resultSet.getString(VoucherFields.PRODUCTION_NAME.getFieldTitle()),
					startDateTime,
					endDateTime,
					resultSet.getDouble(VoucherFields.PAY_RATE.getFieldTitle())).
					productionCompany(resultSet.getString(VoucherFields.PRODUCTION_COMPANY.getFieldTitle())).
					isPaid(resultSet.getBoolean(VoucherFields.PAID.getFieldTitle()))
					.build();

		} finally {
			close(statement);
		}

	}

	/**
	 * 
	 * @return
	 * @throws SQLException
	 */
	public List<LocalDateTime> getDateTimes() throws SQLException {
		List<LocalDateTime> list = new ArrayList<LocalDateTime>();

		String selectString = String.format("SELECT %s FROM %s", VoucherFields.START_DATE_TIME.getFieldTitle(), TABLE_NAME);
		System.out.println(selectString);

		Statement statement = null;
		ResultSet resultSet = null;
		try {
			Connection connection = Database.getConnection();
			statement = connection.createStatement();

			resultSet = statement.executeQuery(selectString);

			while (resultSet.next()) {
				LocalDateTime date = resultSet.getTimestamp(VoucherFields.START_DATE_TIME.getFieldTitle()).toLocalDateTime();
				list.add(date);
				LOG.debug("Getting date: " + date);
			}
		} finally {
			close(statement);
		}
		return list;
	}

	/**
	 * Creates a voucher object from a database row and returns an ArrayList of the objects
	 * 
	 * @return
	 */
	public List<Voucher> getAllVouchers() {

		List<Voucher> list = new ArrayList<Voucher>();

		String selectString = "SELECT * FROM " + TABLE_NAME;

		Statement statement = null;
		ResultSet resultSet = null;

		try {
			Connection connection = Database.getConnection();
			statement = connection.createStatement();
			resultSet = statement.executeQuery(selectString);

			while (resultSet.next()) {

				LocalDateTime startDateTime = resultSet.getTimestamp(VoucherFields.START_DATE_TIME.getFieldTitle()).toLocalDateTime();
				LocalDateTime endDateTime = resultSet.getTimestamp(VoucherFields.END_DATE_TIME.getFieldTitle()).toLocalDateTime();

				list.add(new Voucher.Builder(resultSet.getString(VoucherFields.PRODUCTION_NAME.getFieldTitle()),
						startDateTime,
						endDateTime,
						resultSet.getDouble(VoucherFields.PAY_RATE.getFieldTitle())).
						productionCompany(resultSet.getString(VoucherFields.PRODUCTION_COMPANY.getFieldTitle())).
						isPaid(resultSet.getBoolean(VoucherFields.PAID.getFieldTitle()))
						.build());
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} finally {
			close(statement);
		}
		return list;

	}

	@Override
	public void create() throws SQLException {
		// TODO Auto-generated method stub

	}

}
