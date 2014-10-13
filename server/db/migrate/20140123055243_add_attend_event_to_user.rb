class AddAttendEventToUser < ActiveRecord::Migration
  def change
    add_column :users, :attend, :integer
  end
end
