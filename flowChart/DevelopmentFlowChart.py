from graphviz import Digraph

# The relationships between the various derived implementations of the btree machine

dot = Digraph()
dot.attr(label='A custom CPU for the  Btree algorithm versus a generic CPU\n ')
dot.attr(labelloc='t')
dot.attr(fontsize='24')

dot.node('B',    'The Btree algorithm\nin normal Java',               fontcolor='darkGreen', style='bold', fontsize='20')
dot.node('BS',   'BtreeStuck\nfixed key/value stack')
dot.node('BSS',  'BtreeStuckStatic\nreduced use of new')
dot.node('BSML', 'BtreeSML\nstatic bit memory')
dot.node('BSP',  'BtreeSP\ntransactional')
dot.node('BSA',  'BtreeSA\ntransaction in bit memory')
dot.node('BPA',  'BtreePA\npseudo assembler')
dot.node('BDM',  'BtreeDM\nroutable Verilog')
dot.node('BSF',  'BtreeSF\nThe CUSTOM cpu',                          fontcolor='red',      style='bold', fontsize='20')
dot.node('BAM',  'BtreeBam\non a basic array machine')
dot.node('BAN',  'BtreeBan\nreduced Java');
dot.node('BAP',  'BtreeBap\nThe GENERIC cpu',                        fontcolor='darkblue', style='bold', fontsize='20')

dot.node('S',    'Stuck\nfixed key/value stack in normal Java')
dot.node('SS',   'StuckStatic\nreduced use of new')
dot.node('SSML', 'StuckSML\nstatic bit memory')
dot.node('SSP',  'StuckSP\ntransactional')
dot.node('SSA',  'StuckSA\ntransaction in bit memory')
dot.node('SPA',  'StuckPA\npseudo assembler')
dot.node('SDM',  'StuckDM\nroutable Verilog')

dot.node('MPA',  'MemoryLayoutPA\npseudo assembler')
dot.node('MDM',  'MemoryLayoutDM\nroutable Verilog')
dot.node('LBAM', 'LayoutBam\nBasic Array Machine')
dot.node('LBAN', 'Ban\nBasic Array Machine\nwith one register')

dot.edge('B',    'BS',   color='red')
dot.edge('BS',   'BSS',  color='red')
dot.edge('BSS',  'BSML', color='red')
dot.edge('BSML', 'BSP',  color='red')
dot.edge('BSP',  'BSA',  color='red')
dot.edge('BSA',  'BPA',  color='red')
dot.edge('BPA',  'BDM',  color='red')
dot.edge('BDM',  'BSF',  color='red')
dot.edge('B',    'BAM',  color='darkBlue')
dot.edge('BAM',  'BAN',  color='darkBlue')
dot.edge('BAN',  'BAP',  color='darkBlue')

dot.edge('S',    'SS'   , arrowhead='diamond', color='grey')
dot.edge('SS',   'SSML' , arrowhead='diamond', color='grey')
dot.edge('SSML', 'SSP'  , arrowhead='diamond', color='grey')
dot.edge('SSP',  'SSA'  , arrowhead='diamond', color='grey')
dot.edge('SSA',  'SPA'  , arrowhead='diamond', color='grey')
dot.edge('SPA',  'SDM'  , arrowhead='diamond', color='grey')

dot.edge('MPA',  'MDM'  , arrowhead='diamond', color='grey')

dot.edge('S',    'BS'   , arrowhead='diamond', color='grey')
dot.edge('SS',   'BSS'  , arrowhead='diamond', color='grey')
dot.edge('SSML', 'BSML' , arrowhead='diamond', color='grey')
dot.edge('SSP',  'BSP'  , arrowhead='diamond', color='grey')
dot.edge('SSA',  'BSA'  , arrowhead='diamond', color='grey')
dot.edge('SPA',  'BPA'  , arrowhead='diamond', color='grey')
dot.edge('SDM',  'BDM'  , arrowhead='diamond', color='grey')

dot.edge('MPA',  'SPA'  , arrowhead='diamond', color='grey')
dot.edge('MPA',  'BPA'  , arrowhead='diamond', color='grey')
dot.edge('MDM',  'SDM'  , arrowhead='diamond', color='grey')
dot.edge('MDM',  'BDM'  , arrowhead='diamond', color='grey')

dot.edge('LBAM', 'BAM'  , arrowhead='diamond', color='grey')
dot.edge('LBAM', 'BAN'  , arrowhead='diamond', color='grey')
dot.edge('LBAM', 'LBAN' , arrowhead='diamond', color='grey')
dot.edge('LBAN', 'BAP'  , arrowhead='diamond', color='grey')

dot.render('DevelopmentFlowChart', format='png', cleanup=True)                  # Display
dot.view('flowchart_output')
