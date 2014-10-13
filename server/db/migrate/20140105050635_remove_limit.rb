class RemoveLimit < ActiveRecord::Migration
  def change
  	change_column :events, :admin, :integer, limit: nil
  end
end
