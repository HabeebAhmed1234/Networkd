class ChangeAdmin < ActiveRecord::Migration
  def change
    change_column :events, :admin, :integer
  end
end
