class ChangeLinkedInIdtoString < ActiveRecord::Migration
  def change
  	change_column :notes, :linkedin_id, :string
  end
end
